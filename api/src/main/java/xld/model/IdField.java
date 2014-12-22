package xld.model;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;

/*
	Special field for holding snowflake id-set
	in database this is a bigint value, but on client side (and in every json) must be string,
	client side json can't handle so long integers!

*/
public class IdField extends Field {
	
	
	public IdField(ModelBase model, String fieldName) {
		super(model, fieldName);
	}
	
	@Override 
	public Field getClone(ModelBase model) {
		return new IdField(model, getFieldName());
	}
	
	public boolean isPrimaryKey() {
		return true;
	}
	
	public boolean isPrimaryKeyLive(ModelBase.Row row) {
		return row.get(fieldName) != null;
	}
	

	/* Add value to json object */
	public void addToJson(ModelBase.Row row, JsonObject jrow) {
		if (row == null)
			model.node.error("row is null!!!!");
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
