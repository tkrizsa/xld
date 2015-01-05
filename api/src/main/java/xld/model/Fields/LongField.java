package xld.model.fields;

import xld.model.ModelBase;

public class LongField extends Field {
	
	
	public LongField(ModelBase model, String fieldName) {
		super(model, fieldName);
	}
	
	@Override 
	public Field getClone(ModelBase model) {
		return new LongField(model, getFieldName());
	}
	

	@Override
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
		return super.toString() + "-long";
	}


}
