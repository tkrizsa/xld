package xld.model;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;



public abstract class Field {

	protected String fieldName;
	protected Model model;
	
	public Field(Model model, String fieldName) {
		this.model = model;
		this.fieldName = fieldName;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public boolean isPrimaryKey() {
		return false;
	}

	public boolean isPrimaryKeyLive(Model.Row row) {
		return false;
	}

	public abstract void addToJson(Model.Row row, JsonObject jrow);
	public abstract void addToJson(Model.Row row, JsonArray jrow);
	public abstract void getFromJson(Model.Row row, JsonObject jrow);
	public abstract void getFromJson(Model.Row row, JsonArray jrow, int ix);
	
	public abstract Object parse(Object val);

	public void set(Model.Row row, Object val) {
		row.set(fieldName, parse(val));
	}
		
}

