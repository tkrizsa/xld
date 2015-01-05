package xld.model.fields;

import xld.model.ModelBase;

/*
	Special field for holding snowflake id-set
	in database this is a bigint value, but on client side (and in every json) must be string,
	client side json can't handle so long integers!

*/
public class IdField extends LongField {
	
	
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
	
	@Override
	public String toString() {
		return super.toString() + "-id";
	}


}
