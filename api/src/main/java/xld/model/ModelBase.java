package xld.model;


import xld.model.Field;
import xld.node.Node;
import xld.node.ApiHandler;

import xld.model.IdField;
import xld.model.StringPropField;
import xld.model.ReferenceField;


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
	protected String tableName;
	protected String KEY_SEPARATOR = ",";
	
	protected List<Long> newIds;			/* holds new sipmleflake id's asked by 'fillIds' function, to use later in insert sql statements */
	private String versionString = ""; 		/* included in base of md5 hash, made to check install versions, change it if no field changed, but want to run an install script */
	
	// constructor
	public ModelBase(Node node) {
		this.node = node;
	}
	
	public ModelBase createNew() {
		try {
			return this.getClass().getConstructor(Node.class).newInstance(new Object[] {node});
		} catch (Exception ex) {
			node.error("Exception create class : " + this.getClass().toString());
			node.error(ex.getMessage());
		}
		return null;
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
	
	public static ModelBase createModel(Node node, Class<? extends Model> mt) {
		ModelBase model = null;
		try {
			model = mt.getConstructor(Node.class).newInstance(node);
		} catch (Exception ex) {
			node.error("==== createModel error ====");
			node.error(ex.getMessage());
		}
		return model;	
	}
	
	public ModelBase createModel( Class<? extends Model> mt) {
		return createModel(node, mt);
	}
	
	// versionString
	public void setVersionString(String versionString) {
		this.versionString = versionString;
	}
	
	public String getVersionString() {
		return versionString;
	}
	
	
	// fields
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
	
	public ReferenceField fieldAddReference(String fieldName, Class<? extends Model> referenceModel) {
		ReferenceField f = new ReferenceField(this, fieldName, referenceModel);
		fieldAdd(f);
		
		String rModelId = getModelId(referenceModel);
		expandAdd(rModelId, referenceModel, f);
		
		return f;
	}
	

	
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
	
	
	// ----------- expands ------------
	protected static class Expand {
		String 		expandKey;
		Class<? extends Model> modelClass;
		List<Field> foreignKeys;
		ModelBase 	model;
		boolean 	viewOnly = false;
	}
	protected List<Expand> expands = new ArrayList<Expand>();
	protected List<Expand> currExpands = new ArrayList<Expand>();
	
	
	public void expandAdd(String expandKey, Class<? extends Model> modelClass, Field foreignKey) {
		Expand e = new Expand();
		e.expandKey 	= expandKey;
		e.modelClass 	= modelClass;
		e.foreignKeys 	= new ArrayList<Field>();
		e.foreignKeys.add(foreignKey);
		foreignKey.setExpand(e);
		expands.add(e);
	}
	
	public Expand expandByKey(String expandKey) {
		for (Expand e : expands) {
			if (e.expandKey.equals(expandKey))
				return e;
		}
		return null;
	}
	
	public void setCurrExpand(String currExpandStr) throws Exception {
		String[] cesa = currExpandStr.split(",");
		for (int i = 0; i < cesa.length; i++) {
			String ces = cesa[i];
			boolean viewOnly = false;
			if (ces.endsWith("*")) {
				viewOnly = true;
				ces = ces.substring(0, ces.length()-1);
			}
			Expand e = expandByKey(ces);
			if (e == null) {
				throw new Exception("Unknown expand key in model '" + getModelId() + "' : '" + ces + "'");
			}
			e.model = createModel(e.modelClass);
			currExpands.add(e);
			if (viewOnly) {
				e.viewOnly = true;
				for (Field f : e.model.fields) {
					Field ff = fieldByName(f.getFieldName());
					if (ff == null) {
						f.cloneToMasterView(this);
					}
				}
			} 
		}
	}

	public boolean isFieldExpanded(Field f) {
		for(Expand e : currExpands) {
			if (!e.viewOnly && e.foreignKeys.contains(f)) 
				return true;
		}
		return false;
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
		JsonObject jres = new JsonObject();
		JsonArray jrows = new JsonArray();
		jres.putArray("rows", jrows);
		for (Row row : rows) {
			JsonObject jrow = jsonGetRow(row);
			for (Expand e : currExpands) {
				if (!e.viewOnly) {
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
		return jres.toString();
	}
	
	public JsonObject jsonGetRow(Row row) {
		JsonObject jrow = new JsonObject();
		for (Field field : fields) {
			if (isFieldExpanded(field))
				continue;
			field.addToJson(row, jrow);
		}
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
		clear();
		JsonArray jrows = jdata.getArray("rows");
		for (int ix = 0; ix < jrows.size(); ix++) {
			JsonObject jrow = jrows.get(ix);
			node.info(jrow);
			Row row = rowAdd();
			loadFromJsonRow(row, jrow);
		}
		node.info("------------------------- LOAD FROM JSON RESULT  --------------------------------");
		node.info(jsonGet());
		node.info("------------------------- LOAD FROM JSON RESULT  --------------------------------");
	}
	
	public void loadFromJsonRow(Row row, JsonObject jrow) {
		for (Expand e : currExpands) {
			if (e.viewOnly)
				continue;
			Row erow = e.model.rowAdd();		
			JsonObject jerow = jrow.getObject(e.expandKey);
			if (jerow != null) {
				row.addExpandedRow(e, erow);
				e.model.loadFromJsonRow(erow, jerow);
			}
		}
		
		for (Field f : fields) {
			if (!isFieldExpanded(f)) {
				f.getFromJson(row, jrow);
			} else {
				Expand e = f.getExpand();
				row.set(f.getFieldName(), row.getExpandedRow(e).get(f.getFieldName()));
			}
		}
		
	}
	
	/* ===================================================== SQL ====================================================== */
	public String keySqlWhere(String keys, String tablePrefix) throws Exception {
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
			where += tablePrefix + "`" + field.getFieldName() + "` = '" + keysa[i] + "'";
			i++;
		}
		return where + ") ";
	}
	
	
	
	public void sqlLoadList(final ApiHandler apiHandler) {
		sqlLoadList(apiHandler, null);
	}
	
	public void sqlLoadList(final ApiHandler apiHandler, String keys) {
		StringBuilder s = new StringBuilder();
		s.append("SELECT \r\n\t");
		boolean firstField = true;
		for (Field field : fieldGetSqlFieldList()) {
			s.append((firstField?"":", ") + "t0.`" + field.getFieldName() + "`");
			firstField = false;
		}
		int ti = 1;
		for (Expand e : currExpands) {
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
			s.append("LEFT JOIN `" + e.model.getTableName() + "` AS t" + ti + " ON ");
			boolean ffirst = true;
			for (Field f : e.foreignKeys) {
				s.append((ffirst?"":" AND ")+" t" + ti + ".`" + f.getFieldName() + "` = t0.`" + f.getFieldName() + "`");
				ffirst = false;
			}
			s.append("\r\n");
			ti++;
		}
		if (keys != null) {
			try {
				s.append("WHERE " + keySqlWhere(keys, "t0."));
			} catch (Exception ex) {
				apiHandler.replyError(ex);
			}
		}
		
		sqlLoad(s.toString(), apiHandler);
	}
	
	public void sqlLoadByKeys(String keys, final ApiHandler apiHandler) {
		sqlLoadList(apiHandler, keys);
	}
	
	
	private void sqlJsonRead(JsonObject json) {
		/* save Current Sql Field List into a list from this model */
		List<Field> cfs = fieldGetSqlFieldList();
		/* save the same, for all the expands */
		List<List<Field>> cefs = new ArrayList<List<Field>>();
		for (Expand e : currExpands) {
			cefs.add(e.model.fieldGetSqlFieldList());
			
		}
		
		
		JsonArray jresults = json.getArray("results");
		Iterator<Object> jresultIt = jresults.iterator();
		while (jresultIt.hasNext()) {
			JsonArray jrow = (JsonArray)jresultIt.next();
			Row row = rowAdd();
			int fix = 0;
			for (Field field : cfs) {
				field.getFromJson(row, jrow, fix);
				fix++;
			}
			int eix = 0;
			for (Expand e : currExpands) {
				if (e.viewOnly) {
					for (Field field : cefs.get(eix)) {
						Field thisField = field.getMasterView();
						if (thisField != null) {
							thisField.getFromJson(row, jrow, fix);
						}
						fix++;
					}
				
				} else {
					Row erow = e.model.rowAdd();
					for (Field field : cefs.get(eix)) {
						field.getFromJson(erow, jrow, fix);
						fix++;
					}
					row.addExpandedRow(e, erow);
				}
				eix ++;
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
	
	
	public void sqlLoad(final String query,  final ApiHandler apiHandler) {
	
		node.info("------------------------------------------------------------");
		node.info(query);
		node.info("------------------------------------------------------------");
	
		clear();
				
		JsonObject q = new JsonObject();
		q.putString("action", "raw");
		q.putString("command", query);
		
		final ModelBase thisModel = this;
		
		node.eb().send("xld-sql-persist", q, new Handler<Message<JsonObject>>() {
			public void handle(final Message<JsonObject> ar) {
				if (!("ok".equals(ar.body().getString("status")))) {
					node.error(query);
					node.error(ar.body().getString("message"));
					apiHandler.replyError(ar.body().getString("message"));
					return;
				}
				try {
					sqlJsonRead(ar.body());
				} catch (Exception ex) {
					apiHandler.replyError(ex);
				}
				apiHandler.handle();
			}
		});
	}
	

	
	public void sqlSave(final ApiHandler apiHandler) {
		final ModelBase thisModel = this;
		node.info("===================== save =========================");
		fillIds(new ApiHandler(apiHandler) {
			public void handle() {
				SqlRunner sql = new SqlRunner(node);
				
				Field key = null;
				for (Field field : fields) {
					if (field.isPrimaryKey()) {
						key = field;
						break;
					}
				}
				
				
				for (Row row : rows) {
					if (row.isLive()) {
						sql.update(thisModel, row);
					} else {
						if (key != null) {
							long id = newIds.remove(0);
							key.set(row, id);
						}
						sql.insert(thisModel, row);
					}
				}
			
				sql.run(apiHandler);
			}
		});
	}
	
	public void fillIds(final ApiHandler apiHandler) {
		Field key = null;
		for (Field field : fields) {
			if (field.isPrimaryKey()) {
				key = field;
				break;
			}
		}
		if (key == null) {
			apiHandler.handle();
			return;
		}
		int idCount = 0;
		for (Row row : rows) {
			if (!row.isLive()) {
				idCount++;
			}
		}
		if (idCount == 0) {
			apiHandler.handle();
			return;
		}
		
		final int idCountFinal = idCount;
		final Field keyFinal = key;
		
		JsonObject r = new JsonObject();
		r.putNumber("idCount", idCount);
		node.eb().send("xld-getid", r, new ApiHandler(apiHandler) {
			public void handle() {
				JsonArray jids = getMessage().body().getArray("ids");
				
				if (jids.size() != idCountFinal) {
					replyError("ID count missmatch!");
					return;
				}
				
				if (newIds == null) {
					newIds = new LinkedList<Long>();
				}
				for (int i = 0; i < idCountFinal; i++) {
					long id = jids.get(i);
					newIds.add(id);
				}
				
				
				apiHandler.handle();
			}
		});
	
	}
	
	
	/* ===================================================== Row ====================================================== */
	
	public static class Row {
		private final ModelBase model;
		private Object[] data;
		private Map<Expand,Row> expands;
		
		
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
			for (Field field : model.fields) {
				if (!field.isPrimaryKey())
					continue;
				return field.isPrimaryKeyLive(this);
			
			}
			return false;
		}
		
		public void addExpandedRow(Expand expand, Row row) {
			if (expands == null)
				expands = new HashMap<Expand, Row>();
				
			expands.put(expand, row);
		}
		
		public Row getExpandedRow(Expand expand) {
			return expands == null ? null : expands.get(expand);
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