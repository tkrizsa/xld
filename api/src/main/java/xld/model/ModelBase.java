package xld.model;


import xld.node.Node;
import xld.node.ApiHandler;

import xld.model.fields.*;

import java.lang.reflect.InvocationTargetException;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.Iterable;
import java.lang.IndexOutOfBoundsException;
import javax.naming.OperationNotSupportedException;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;

import java.util.HashMap;
import java.util.Map;


public class ModelBase implements Iterable {


	public static String MODEL_ID = "???";

	protected Node node;
	protected List<Field> fields = new ArrayList<Field>();
	protected List<Row> rows = new ArrayList<Row>();
	
	protected ModelBase parent; 		// another model created me as expansion
	protected Expand parentExpand;		// another model created me as expansion and this is parent's expand that points to me
	protected Expand masterExpand;		// another model created me as expansion and it is my master and this is my expand what points back to parent
	
	protected String tableName;
	protected String KEY_SEPARATOR = ",";
	
	//protected List<Long> newIds;			/* holds new sipmleflake id's asked by 'fillIds' function, to use later in insert sql statements */
	private String versionString = ""; 		/* included in base of md5 hash, made to check install versions, change it if no field changed, but want to run an install script */
	
	// --------------------------- constructor ---------------------------
	public ModelBase(Node node) {
		this(node, null);
	}
	
	public ModelBase(Node node, ModelBase parent) {
		this.node = node;
		this.parent = parent;
	}


	public static ModelBase createModel(Node node, Class<? extends Model> mt) {
		return ModelBase.createModel(node, mt, null);
	}
	
	public static ModelBase createModel(Node node, Class<? extends Model> mt, ModelBase parent) {
		ModelBase model = null;
		try {
			model = mt.getConstructor(Node.class, ModelBase.class).newInstance(node, parent);
		} catch (InvocationTargetException ex) {
			node.error("==== createModel error ====");
			node.error(ex.getCause());
			ex.printStackTrace();
		} catch (Exception ex) {
			node.error("==== createModel error ====");
			node.error(ex.toString());
			ex.printStackTrace();
		}
		return model;	
	}
	
	public ModelBase createModel(Class<? extends Model> mt) {
		return createModel(node, mt);
	}
	
	public ModelBase createModel(Class<? extends Model> mt, ModelBase parent) {
		return createModel(node, mt, parent);
	}
	
	
	// tableName
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	//modelId
	private String modelId = null;
	
	public String getModelId() {
		if (modelId != null) {
			return modelId;
		}
		modelId = "xxx";
		try {
			Class<? extends ModelBase> mt = this.getClass();
			java.lang.reflect.Field f = mt.getField("MODEL_ID");
			modelId = f.get(null).toString();
		} catch (Exception ex) {
			node.error("==== error ====");
			node.error(ex.getMessage());
		}
		return modelId;
	}
	
	public static String getModelId(Class<? extends Model> mt) {
		String modelId = "xxx";
		try {
			java.lang.reflect.Field f = mt.getField("MODEL_ID");
			modelId = f.get(null).toString();
		} catch (Exception ex) {
		}
		return modelId;
	}
	
	public String getModelIdPlural() {
		return this.getModelId() + "s";
	}
	
	
	// versionString
	public void setVersionString(String versionString) {
		this.versionString = versionString;
	}
	
	public String getVersionString() {
		return versionString;
	}
	
	
	// ------------------------------- fields -------------------------------
	public List<Field> getFields() {
		return fields;
	}
	
	public Field fieldAdd(Field f) {
		fields.add(f);
		return f;
	}
	
	public IdField fieldAddId(String fieldName) {
		IdField f = new IdField(this, fieldName);
		fieldAdd(f);
		return f;
	}
	
	public StringPropField fieldAddStringProp(String fieldName, int maxLength) {
		StringPropField f = new StringPropField(this, fieldName, maxLength);
		fieldAdd(f);
		return f;
	}
	public AmountPropField fieldAddAmountProp(String fieldName) {
		AmountPropField f = new AmountPropField(this, fieldName);
		fieldAdd(f);
		return f;
	}
	
	public MoneyField fieldAddMoney(String fieldName) {
		MoneyField f = new MoneyField(this, fieldName);
		fieldAdd(f);
		return f;
	}

	public ReferenceField fieldAddReference(String fieldName, Class<? extends Model> referenceModel) {
		return fieldAddReference(fieldName, referenceModel, getModelId(referenceModel));
	}
	
	public ReferenceField fieldAddReference(String fieldName, Class<? extends Model> referenceModel, String expandKey) {
		ReferenceField f = new ReferenceField(this, fieldName, referenceModel);
		fieldAdd(f);
		expandAddReference(expandKey, referenceModel, f, ExpandType.REFERENCE);
		return f;
	}
	
	public MasterField fieldAddMaster(String fieldName, Class<? extends Model> referenceModel) {
		return fieldAddMaster(fieldName, referenceModel, getModelId(referenceModel));
	}
	
	public MasterField fieldAddMaster(String fieldName, Class<? extends Model> referenceModel, String expandKey) {
		MasterField f = new MasterField(this, fieldName, referenceModel);
		fieldAdd(f);
		expandAddReference(expandKey, referenceModel, f, ExpandType.MASTER);
		return f;
	}
	
	// --------------
	

	
	public int fieldIx(String fieldName) {
		int ix = -1;
		for (Field f : fields) {
			ix++;
			if (f.getFieldName().equals(fieldName))
				return ix;
		
		}
		return -1;
	}
	
	public Field fieldByName(String fieldName) {
		for (Field f : fields) {
			if (f.getFieldName().equals(fieldName))
				return f;
		
		}
		return null;
	}
	
	public int fieldCount() {
		return fields.size();
	
	}
	
	
	protected List<Field> fieldGetSqlFieldList() {
		List<Field> sf = new ArrayList<Field>();
		for (Field field : fields) {
			if (field.getSqlField()) {
				sf.add(field);
			}
		}
		return sf;
	}
	
	
	// ------------------------- expands ------------------------------
	
	public static class KeyPair {
		Field foreignKey;
		Field referencedKey;
		
		public KeyPair(Field foreignKey, Field referencedKey) {
			this.foreignKey    = foreignKey;
			this.referencedKey = referencedKey;
		}
	}

	public static enum ExpandType {
		REFERENCE, MASTER, DETAIL
	}

	
	public static class Expand {
		ExpandType 				type;
		String 					expandKey;
		Class<? extends Model> 	modelClass;
		List<KeyPair> 			keys;
		ModelBase 				model;
		boolean					currExpanded = false;
		
		public boolean isCurrExpanded() {
			return currExpanded;
		}
		
	}
	
	List<Expand> expands = new ArrayList<Expand>();
	List<Expand> currExpands = new ArrayList<Expand>();
	String currExpandStr = "";
	
	
	public void expandAddReference(String expandKey, Class<? extends Model> modelClass, Field foreignKey, ExpandType type) {
		Expand e = new Expand();
		e.type 			 = type;
		e.expandKey 	 = expandKey;
		e.modelClass 	 = modelClass;
		e.keys		 	 = new ArrayList<KeyPair>();
		
		if (parent != null && modelClass == parent.getClass()) {
			e.model = parent;
		} else {
			e.model = createModel(e.modelClass, this);
			e.model.parentExpand = e;
			e.model.setCurrExpand(currExpandStr);
		}
		
		
		
		for (Field f : e.model.fields) {
			if (f.isPrimaryKey()) {
				e.keys.add(new KeyPair(foreignKey, f));
			}
		}
		
		foreignKey.setExpand(e);
		expands.add(e);
	}
	
	public void expandAddDetail(String expandKey, Class<? extends Model> modelClass, String foreignKeyFieldName) {
		Expand e = new Expand();
		e.type 			 = ExpandType.DETAIL;
		e.expandKey 	 = expandKey;
		e.modelClass 	 = modelClass;
		e.keys		 	 = new ArrayList<KeyPair>();
		
		if (parent != null && modelClass == parent.getClass()) {
			e.model = parent;
		} else {
			e.model = createModel(e.modelClass, this);
			e.model.parentExpand = e;
			
			// masterExpand is the expand in a master - detail relationship what points to master IF AND ONLY master is the parent
			for (Expand ex : e.model.expands) {
				if (ex.model == this) {
					e.model.masterExpand = ex;
					break;
				}
			}
		}
		Field foreignKey = e.model.fieldByName(foreignKeyFieldName);
		if (foreignKey == null) {
			node.error("Foreign key field " + foreignKeyFieldName + " not found in detail type expansion!");
			return;
		}
		for (Field f : fields) {
			if (f.isPrimaryKey()) {
				e.keys.add(new KeyPair(foreignKey, f));
			}
		}
		expands.add(e);
	}
	
	
	
	
	
	
	
	public Expand expandByKey(String expandKey) {
		for (Expand e : expands) {
			if (e.expandKey.equals(expandKey))
				return e;
		}
		return null;
	}
	
	public void setCurrExpand(String currExpandStr)  {
		this.currExpandStr = currExpandStr;
		String[] cesa = currExpandStr.split(",");
		for (int i = 0; i < cesa.length; i++) {
			String ces = cesa[i];
			Expand e = expandByKey(ces);
			if (e == null) {
				//throw new Exception("Unknown expand key in model '" + getModelId() + "' : '" + ces + "'");
			} else {
				e.currExpanded = true;
				currExpands.add(e);
				if (e.model.parent == this)
					e.model.setCurrExpand(currExpandStr);
			}
		}
	}
		
	
	// ----------- row functions ------------
	
	
	public void clear() {
		rows = new ArrayList<Row>();
	}
	
	public Row rowAdd() {
		Row r = new Row(this);
		
		
		rows.add(r);
		return r;
	}
	
	public boolean empty() {
		return rows.size() == 0;
	}
	
	/* ===================================================== JSON ====================================================== */
	
	public String jsonGet() {
		return jsonGetObject().toString();
	}
	
	public JsonObject jsonGetObject() {
		return jsonGetObject(rows);
	}
	
	public JsonObject jsonGetObject(List<Row> rowList) {
		JsonObject jres = new JsonObject();
		JsonArray jrows = new JsonArray();
		jres.putArray("rows", jrows);
		if (rowList == null) {
			return jres;
		}
		
		for (Row row : rowList) {
			JsonObject jrow = jsonGetRow(row);
			for (Expand e : currExpands) {
				if (e.type == ExpandType.DETAIL) {
				
					jrow.putObject(e.expandKey, e.model.jsonGetObject(row.getDetailRows(e)));
				} else {
					Row erow = row.getExpandedRow(e);
					if (erow != null) {
						jrow.putObject(e.expandKey, e.model.jsonGetRow(erow));
					} else {
						jrow.putObject(e.expandKey, null);
					}
				}
			}
			jrows.addObject(jrow);
		}
		return jres;
	}
	
	public JsonObject jsonGetRow(Row row) {
		JsonObject jrow = new JsonObject();
		for (Field field : fields) {
			if (field.isFieldExpanded()) 
				continue;
			field.addToJson(row, jrow);
		}
		jrow.putBoolean("_live", row.live);
		jrow.putBoolean("_deleted", row.deleted);
		jsonAddLinks(row, jrow);
		return jrow;
	}
	
	protected void jsonAddLinks(Row row, JsonObject jrow) {
		String keys = keysToString(row);
		if ("".equals(keys))
			return;
	
		JsonObject jself = new JsonObject();
		jself.putString("href", "/api/" + getModelIdPlural() + "/" + keys);
		jrow.putObject("self", jself);
		
		JsonObject jgui = new JsonObject();
		jgui.putString("href", "/" + getModelIdPlural() + "/" + keys);
		jrow.putObject("gui", jgui);
	}
	
	protected String keysToString(Row row) {
		String keys = "";
		for(Field field : fields) {
			if (!field.isPrimaryKey())
				continue;
			Object val = row.get(field.getFieldName());
			if (val == null)
				continue;
			if (!"".equals(keys))
				keys += KEY_SEPARATOR;
			keys += val;
		}
		return keys;
	}
	
	/* just a trial, not used yet. not sure if useful. consider no return value! */
	public void loadFromJson(JsonObject jdata, ApiHandler apiHandler) {
		try {
			loadFromJson(jdata);
		} catch (Exception ex) {
			apiHandler.replyError(ex.toString());
		}
	}
	
	
	public void loadFromJson(JsonObject jdata) throws Exception {
		loadFromJson(jdata, null, null);
	}
	
	public void loadFromJson(JsonObject jdata, Expand masterExpand, Row masterRow) throws Exception {
		clear();
		JsonArray jrows = jdata.getArray("rows");
		for (int ix = 0; ix < jrows.size(); ix++) {
			JsonObject jrow = jrows.get(ix);
			node.info(jrow);
			Row row = rowAdd();
			loadFromJsonRow(row, jrow, masterExpand, masterRow);
			if (masterExpand != null) {
				masterRow.addDetailRow(masterExpand, row);
				for (KeyPair kp : masterExpand.keys) {
					// node.info("COPY TO DETAIL");
					// node.info("FROM : " + kp.referencedKey.getFieldName());
					// node.info("TO : " + kp.foreignKey.getFieldName());
					// node.info(kp.referencedKey.get(masterRow));
					// node.info(masterRow.toString());
					kp.foreignKey.set(row, kp.referencedKey.get(masterRow));
				}
			}
		}
		// node.info("------------------------- LOAD FROM JSON RESULT : " + getModelId()  + " --------------------------------");
		// node.info(jsonGet());
		// node.info("------------------------- /LOAD FROM JSON RESULT  --------------------------------");
	}
	
	public void loadFromJsonRow(Row row, JsonObject jrow) throws Exception {
		loadFromJsonRow(row, jrow, null, null);
	}
	
	public void loadFromJsonRow(Row row, JsonObject jrow, Expand masterExpand, Row masterRow) throws Exception {
		row.live 	= jrow.getBoolean("_live", false);
		row.deleted = jrow.getBoolean("_deleted", false);
	
	
		for (Expand e : currExpands) {
			if (e.type != ExpandType.DETAIL) {
				Row erow = e.model.rowAdd();		
				JsonObject jerow = jrow.getObject(e.expandKey);
				if (jerow != null) {
					row.addExpandedRow(e, erow);
					e.model.loadFromJsonRow(erow, jerow);
				}
			}
		}
		
		for (Field f : fields) {
			if (!f.isFieldExpanded()) {
				f.getFromJson(row, jrow);
			} else {
				Expand e = f.getExpand();
				for (KeyPair kp : e.keys) {
					Row erow = row.getExpandedRow(e);
					if (erow !=  null)
						kp.foreignKey.set(row, kp.referencedKey.get(erow));
				}
			}
		}
		
		for (Expand e : currExpands) {
			if (e.type == ExpandType.DETAIL) {
				JsonObject jdata = jrow.getObject(e.expandKey);
				e.model.loadFromJson(jdata, e, row);
			} 
		}
		
		
	}
	
	/* ===================================================== SQL ====================================================== */
	public String keySqlWhere(String keys, String tablePrefix, List<Object> values) throws Exception {
		if (tablePrefix == null)
			tablePrefix = "";
		String where = " (";
		String[] keysa = keys.split(KEY_SEPARATOR);
		int i = 0;
		for(Field field : fields) {
			if (!field.isPrimaryKey())
				continue;
			if (i>0)
				where += " AND ";
			where += tablePrefix + "`" + field.getFieldName() + "` = ? ";
			values.add(keysa[i]);
			i++;
		}
		return where + ") ";
	}
	
	
	
	public void sqlLoadList(final ApiHandler apiHandler) {
		sqlLoadList(apiHandler, null);
	}
	
	public void sqlLoadList(final ApiHandler apiHandler, String keys) {
		final SqlRunner sql = new SqlRunner(node);
		try {
			for (Expand e : currExpands) {
				if (e.type != ExpandType.DETAIL)
					continue;
				e.model.sqlLoadStatement(sql, keys, e.expandKey);
			}
			sqlLoadStatement(sql, keys, "_master");
		} catch (Exception ex) {
			apiHandler.replyError(ex);
			return;
		}
		
		sql.run(new ApiHandler(apiHandler) {
			public void handle() {
				try {
					sqlJsonRead(sql.getResult("_master"));
				} catch (Exception ex) {
					apiHandler.replyError(ex);
				}
				
				for (Expand e : currExpands) {
					if (e.type != ExpandType.DETAIL)
						continue;
					e.model.sqlJsonRead(sql.getResult(e.expandKey));
				}				
				
				apiHandler.handle();				
			}
		});
		
	}
	
	public void sqlLoadByKeys(String keys, final ApiHandler apiHandler) {
		sqlLoadList(apiHandler, keys);
	}
	
	
	
	public void sqlLoadStatement(SqlRunner sql, String keys) throws Exception {
		sqlLoadStatement(sql, keys, null);
	}
	
	public void sqlLoadStatement(SqlRunner sql, String keys, String resultKey) throws Exception {
		List<Object> values = new ArrayList<Object>();
	
		StringBuilder s = new StringBuilder();
		s.append("SELECT \r\n\t");
		boolean firstField = true;
		for (Field field : fieldGetSqlFieldList()) {
			s.append((firstField?"":", ") + "t0.`" + field.getFieldName() + "`");
			firstField = false;
		}
		int ti = 1;
		for (Expand e : currExpands) {
			if (e.type == ExpandType.DETAIL)
				continue;
			s.append("\r\n\t");
			for (Field field : e.model.fieldGetSqlFieldList()) {
				s.append((firstField?"":", ") + "t" + ti + ".`" + field.getFieldName() + "`");
				firstField = false;
			}
			ti++;
		}
		
		s.append("\r\nFROM `" + getTableName() + "` AS t0 \r\n");
		ti = 1;
		for (Expand e : currExpands) {
			if (e.type == ExpandType.DETAIL)
				continue;
			s.append("LEFT JOIN `" + e.model.getTableName() + "` AS t" + ti + " ON ");
			boolean ffirst = true;
			for (KeyPair kp : e.keys) {
				s.append((ffirst?"":" AND ")+" t" + ti + ".`" + kp.referencedKey.getFieldName() + "` = t0.`" + kp.foreignKey.getFieldName() + "`");
				ffirst = false;
			}
			s.append("\r\n");
			ti++;
		}
		if (parentExpand != null && keys != null) {
			node.info("itt a parentexpand not null!" + this.getModelId());
			if (parent == null)
				node.info("parent = null????");
			s.append("WHERE " + parent.keySqlWhere(keys, "t0.", values));
			
		} else if (keys != null) {
			s.append("WHERE " + keySqlWhere(keys, "t0.", values));
		} 
		sql.prepared(s.toString(), values.toArray(), resultKey);
	}
	

	
	// ----------------------------------------------------------------------------------------------------------
	private void sqlJsonRead(JsonObject json) {
		/* save Current Sql Field List into a list from this model */
		List<Field> cfs = fieldGetSqlFieldList();
		/* save the same, for all the expands */
		List<List<Field>> cefs = new ArrayList<List<Field>>();
		for (Expand e : currExpands) {
			if (e.type == ExpandType.DETAIL)
				continue;
			cefs.add(e.model.fieldGetSqlFieldList());
		}
		
		
		JsonArray jresults = json.getArray("results");
		Iterator<Object> jresultIt = jresults.iterator();
		while (jresultIt.hasNext()) {
			JsonArray jrow = (JsonArray)jresultIt.next();
			Row row = rowAdd();
			row.live = true;
			
			// read own fields
			int fix = 0;
			for (Field field : cfs) {
				field.getFromJson(row, jrow, fix);
				fix++;
			}
			
			// read expanded fields
			int eix = 0;
			for (Expand e : currExpands) {
				if (e.type == ExpandType.DETAIL)
					continue;
				Row erow = e.model.rowAdd();
				erow.live = true;
				for (Field field : cefs.get(eix)) {
					field.getFromJson(erow, jrow, fix);
					fix++;
				}
				row.addExpandedRow(e, erow);
				eix ++;
			}
			
			// link row to parent row if parent model exists and it is my master
			if (masterExpand != null) {
				for (Row parentRow : masterExpand.model.rows) {
					boolean ok = true;
					for (KeyPair kp : masterExpand.keys) {
						if (!(kp.foreignKey.get(row).equals(kp.referencedKey.get(parentRow)))) {
							ok = false;
							break;
						}
					}
					if (ok) {
						//row.addExpandedRow(parentExpand, parentRow);
						parentRow.addDetailRow(parentExpand, row);
						break;
					}
					
				}
			
			}
		}
	}
	
	private void sqlJsonReadByName(JsonObject json) {
		JsonArray jfields = json.getArray("fields");
		
		int[] fmap = new int[fields.size()];
		for (int i = 0; i < fields.size(); i++) 
			fmap[i] = -1;
		
		Iterator<Object> jfieldsIt = jfields.iterator(); 
		{
			int i = 0;
			while (jfieldsIt.hasNext()) {
				String fn = (String)jfieldsIt.next();
				int ix = fieldIx(fn);
				if (ix < 0) {
					i++;
					continue;
				}
				fmap[ix] = i;
				i++;
			}
		}
		
		JsonArray jresults = json.getArray("results");
		Iterator<Object> jresultIt = jresults.iterator();
		while (jresultIt.hasNext()) {
			JsonArray jrow = (JsonArray)jresultIt.next();
			Row row = rowAdd();
			for ( int i = 0; i < fmap.length; i++) {
				if (fmap[i] < 0)
					continue;
				Field field = fields.get(i);
				field.getFromJson(row, jrow, fmap[i]);
				//row.set(i, jrow.get(fmap[i]));
				
			}
		}
	}
	


	// -------------------------------------------- SAVE --------------------------------------------------
	public void sqlSave(final ApiHandler apiHandler) {
		node.info("===================== save =========================");
		fillIds(new ApiHandler(apiHandler) {
			public void handle() {
			
				node.info("------------------------- 2.LOAD FROM JSON RESULT : " + getModelId()  + " --------------------------------");
				node.info(jsonGet());
				node.info("------------------------- /2.LOAD FROM JSON RESULT  --------------------------------");
			
			
				SqlRunner sql = new SqlRunner(node);
				sqlSaveAdd(sql);
				
			
				sql.run(apiHandler);
			}
		});
	}
	
	public void sqlSaveAdd(SqlRunner sql) {
		for (Row row : rows) {
			if (row.isLive()) {
				sql.update(this, row);
			} else {
				sql.insert(this, row);
			}
		}
		
		for (Expand e : currExpands) {
			if (e.type == ExpandType.DETAIL) {
				e.model.sqlSaveAdd(sql);
			}
		}
	
	}
	
	
	public void fillIds(final ApiHandler apiHandler) {
		
		final int idCount = getMissingIdCount();
		
		node.info("IDCOUNT : " + idCount);
		
		if (idCount == 0) {
			apiHandler.handle();
			return;
		}
		
		
		JsonObject r = new JsonObject();
		r.putNumber("idCount", idCount);
		node.eb().send("xld-getid", r, new ApiHandler(apiHandler) {
			public void handle() {
				JsonArray jids = getMessage().body().getArray("ids");
				
				if (jids.size() != idCount) {
					replyError("ID count missmatch!");
					return;
				}
				
				List<Long> newIds = new LinkedList<Long>();
				
				for (int i = 0; i < idCount; i++) {
					long id = jids.get(i);
					newIds.add(id);
				}
				
				putMissingIds(newIds);
				
				apiHandler.handle();
			}
		});
	
	}
	
	public int getMissingIdCount() {
		Field key = null;
		for (Field field : fields) {
			if (field.isPrimaryKey()) {
				key = field;
				break;
			}
		}
		int idCount = 0;
		if (key != null) {
			for (Row row : rows) {
				if (!row.isLive()) {
					idCount++;
				}
			}
		}
		
		for (Expand e : currExpands) {
			node.info("-exp :" + e.expandKey);
			if (parent == null || parent != e.model)  // dont call my parent to avoid infinite loop
				idCount += e.model.getMissingIdCount();
		}
		
		node.info("idcount : " + idCount);
		return idCount;
	}
	
	
	public void putMissingIds(List<Long> newIds) {
		Field key = null;
		for (Field field : fields) {
			if (field.isPrimaryKey()) {
				key = field;
				break;
			}
		}
		if (key != null) {
			for (Row row : rows) {
				if (!row.isLive()) {
					long id = newIds.remove(0);
					key.set(row, id);
				}
			}
		}
		
		for (Expand e : currExpands) {
			if (e.type == ExpandType.DETAIL) {
				for (Row row : rows) {
					List<Row> list = row.getDetailRows(e);
					if (list != null) {
						for (Row drow : list) {
							for (KeyPair kp : e.keys) {
								kp.foreignKey.set(drow, kp.referencedKey.get(row));
							}
						}
					}
				}
			}
		
		
			if (parent == null || parent != e.model) { // dont call my parent to avoid infinite loop
				e.model.putMissingIds(newIds);
			}
		}
	}
	
	
	/* ===================================================== Row ====================================================== */
	
	public static class Row {
		private final ModelBase model;
		private Object[] data;
		private Map<Expand,Row> expands;
		private Map<Expand,List<Row>> details;
		protected boolean live = false;
		protected boolean deleted = false;
		
		
		public Row(ModelBase model) {
			this.model = model;
			this.data = new Object[model.fieldCount()];
		}
	
		public Object get(String fieldName) {
			int ix = model.fieldIx(fieldName);
			if (ix<0)
				return null;
				//throw new IndexOutOfBoundsException();
			return data[ix];
		}

		public Object get(int ix) {
			if (ix<0 || ix >= model.fieldCount())
				return null;
				//throw new IndexOutOfBoundsException();
			return data[ix];
		}
		
		public void set(String fieldName, Object value) {
			int ix = model.fieldIx(fieldName);
			if (ix<0)
				return;
				//throw new IndexOutOfBoundsException();
			data[ix] = value;
		}

		public void set(int ix, Object value) {
			if (ix<0 || ix >= model.fieldCount())
				return;
				//throw new IndexOutOfBoundsException();
			data[ix] = value;
		}
		
		public boolean isLive() {
			return live;
		}
		
		public void addExpandedRow(Expand expand, Row row) {
			if (expands == null)
				expands = new HashMap<Expand, Row>();
				
			expands.put(expand, row);
		}
		
		public Row getExpandedRow(Expand expand) {
			return expands == null ? null : expands.get(expand);
		}
		
		public void addDetailRow(Expand expand, Row row) {
			if (details == null)
				details = new HashMap<Expand, List<Row>>();
			List<Row> list = details.get(expand);
			if (list == null) {
				list = new ArrayList<Row>();
				details.put(expand, list);
			}
			list.add(row);
		}
		
		public List<Row> getDetailRows(Expand expand) {
			return details == null ? null : details.get(expand);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(data.length + " : {");
			boolean first = true;
			for(int i = 0; i < data.length; i++) {
				sb.append((first?"":",") + (data[i]==null?"[null]":data[i].toString()));
				first = false;
			}
			sb.append("}");
			return sb.toString();
		}
		
	}
	
	
	/* ===================================================== ITERATOR ====================================================== */
	


	@Override
	public Iterator<Row> iterator() {
		return rows.iterator();
	}




	


}