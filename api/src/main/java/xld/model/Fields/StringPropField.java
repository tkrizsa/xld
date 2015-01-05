package xld.model.fields;

import xld.model.ModelBase;


public class StringPropField extends Field {
	
	private int maxLength;
	
	public StringPropField(ModelBase model, String fieldName, int maxLength) {
		super(model, fieldName);
		this.maxLength = maxLength;
	}
	
	@Override 
	public Field getClone(ModelBase model) {
		return new StringPropField(model, getFieldName(), maxLength);
	}
	
	@Override
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