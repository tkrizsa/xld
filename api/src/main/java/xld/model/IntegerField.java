package xld.model;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;

public class IntegerField extends Field {
	
	
	public IntegerField(ModelBase model, String fieldName) {
		super(model, fieldName);
	}
	
	public boolean isPrimaryKey() {
		return true;
	}
	
	public boolean isPrimaryKeyLive(ModelBase.Row row) {
		return row.get(fieldName) != null;
	}
	

	public void addToJson(ModelBase.Row row, JsonObject jrow) {
		Object val = row.get(fieldName);
		// in row must be null or Long
		jrow.putNumber(fieldName, val == null ? null : (long)val);
	}
	
	public void getFromJson(ModelBase.Row row, JsonObject jrow) {
		Object val = jrow.getValue(fieldName);
		val = parse(val);
		row.set(fieldName, val);
	}
	
	public void getFromJson(ModelBase.Row row, JsonArray jrow, int ix) {
		row.set(fieldName, parse(jrow.get(ix)));
	}

	public void addToJson(ModelBase.Row row, JsonArray jrow) {
		Object val = parse(row.get(fieldName));
		if (val == null) {
			jrow.add(null);
		} else {
			jrow.addNumber((long)val);
		}
	}
	
	public Object parse(Object val) {
		if (val == null || val instanceof Long) {
			return val == null ? null : val;
		} else if (val instanceof Integer) {
			return new Long((Integer)val);
		} else {
			return Long.parseLong(val.toString());
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + "-id";
	}


}
