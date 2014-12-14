package xld.model;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;



public abstract class Field {

	protected String fieldName;
	protected ModelBase model;
	
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

	public abstract void addToJson(ModelBase.Row row, JsonObject jrow);
	public abstract void addToJson(ModelBase.Row row, JsonArray jrow);
	public abstract void getFromJson(ModelBase.Row row, JsonObject jrow);
	public abstract void getFromJson(ModelBase.Row row, JsonArray jrow, int ix);
	
	public abstract Object parse(Object val);

	public void set(ModelBase.Row row, Object val) {
		row.set(fieldName, parse(val));
	}
	
	@Override
	public String toString() {
		return getFieldName();
	}
		
}

