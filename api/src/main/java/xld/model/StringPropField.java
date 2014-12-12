package xld.model;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;

public class StringPropField extends Field {
	
	private int maxLength;
	
	public StringPropField(Model model, String fieldName, int maxLength) {
		super(model, fieldName);
		this.maxLength = maxLength;
	}
	
	public void addToJson(Model.Row row, JsonObject jrow) {
		String val = (String)row.get(fieldName);
		jrow.putString(fieldName, val);
	}
	
	public void getFromJson(Model.Row row, JsonObject jrow) {
		String val = jrow.getString(fieldName);
		row.set(fieldName, val);
	}
	
	public void getFromJson(Model.Row row, JsonArray jrow, int ix) {
		Object val = jrow.get(ix);
		row.set(fieldName, val == null ? null : val.toString());
	}

	public void addToJson(Model.Row row, JsonArray jrow) {
		String val = (String)row.get(fieldName);
		jrow.addString(val);
	}
	
	public Object parse(Object val) {
		if (val == null || val instanceof String) {
			return val == null ? null : val;
		} else {
			return val.toString();
		}
	}
	

	

}