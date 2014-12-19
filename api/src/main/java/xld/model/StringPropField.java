package xld.model;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;

public class StringPropField extends Field {
	
	private int maxLength;
	
	public StringPropField(ModelBase model, String fieldName, int maxLength) {
		super(model, fieldName);
		this.maxLength = maxLength;
	}
	
	@Override 
	public StringPropField getClone(ModelBase model) {
		return new StringPropField(model, getFieldName(), maxLength);
	}
	
	public void addToJson(ModelBase.Row row, JsonObject jrow) {
		String val = (String)row.get(fieldName);
		jrow.putString(fieldName, val);
	}
	
	public void getFromJson(ModelBase.Row row, JsonObject jrow) {
		String val = jrow.getString(fieldName);
		row.set(fieldName, val);
	}
	
	public void getFromJson(ModelBase.Row row, JsonArray jrow, int ix) {
		Object val = jrow.get(ix);
		row.set(fieldName, val == null ? null : val.toString());
	}

	public void addToJson(ModelBase.Row row, JsonArray jrow) {
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
	
	@Override
	public String toString() {
		return super.toString() + "-string(" + maxLength + ")";
	}

	

}