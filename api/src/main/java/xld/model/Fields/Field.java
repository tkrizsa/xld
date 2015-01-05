package xld.model.fields;

import xld.model.ModelBase;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;



public abstract class Field {

	protected String fieldName;
	protected ModelBase model;
	private boolean sqlField = true;
	
	
	public Field(ModelBase model, String fieldName) {
		this.model = model;
		this.fieldName = fieldName;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public boolean isPrimaryKey() {
		return false;
	}

	public boolean isPrimaryKeyLive(ModelBase.Row row) {
		return false;
	}
	
	public void setSqlField(boolean value) {
		sqlField = false;
	}
	
	public boolean getSqlField() {
		return sqlField;
	}
	
	@Override
	public String toString() {
		return getFieldName();
	}

	// --------------- handle value parsing & conversion -----------------
	public abstract Object parse(Object val);
	
	/* Add value to json object */
	public void addToJson(ModelBase.Row row, JsonObject jrow) {
		Object val = row.get(fieldName);
		jrow.putString(fieldName, val == null ? null : val.toString());
	}

	/* Add value to json array */
	public void addToJson(ModelBase.Row row, JsonArray jrow) {
		Object val = row.get(fieldName);
		jrow.addString(val == null ? null : val.toString());
	}

	/* Read value from json object */
	public void getFromJson(ModelBase.Row row, JsonObject jrow) {
		Object val = jrow.getValue(fieldName);
		val = parse(val);
		row.set(fieldName, val);
	}
	
	/* Read value from json array */
	public void getFromJson(ModelBase.Row row, JsonArray jrow, int ix) {
		row.set(fieldName, parse(jrow.get(ix)));
	}
	

	public void set(ModelBase.Row row, Object val) {
		row.set(fieldName, parse(val));
	}
	
	public Object get(ModelBase.Row row) {
		return row.get(fieldName);
	}
	
	public abstract Field getClone(ModelBase model);
		
	
	// ----------------------- expands -------------------------------
	private ModelBase.Expand expand;			// if field can be expanded, this refers to owner model's Expand object
	
	public void setExpand(ModelBase.Expand expand) {
		this.expand = expand;
	}
	
	public ModelBase.Expand getExpand() {
		return this.expand;
	}
	
	public boolean isFieldExpanded() {
		ModelBase.Expand e = getExpand();
		return (e!=null && e.isCurrExpanded());
	}
	
	
}

