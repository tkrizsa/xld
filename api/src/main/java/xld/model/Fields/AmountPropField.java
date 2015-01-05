package xld.model.fields;

import xld.model.ModelBase;
import java.math.BigDecimal;

public class AmountPropField extends Field {

	protected int precision = 21;
	protected int scale = 3;
	
	public AmountPropField(ModelBase model, String fieldName) {
		super(model, fieldName);
	}
	
	@Override
	public Field getClone(ModelBase model) {
		return new AmountPropField(model, getFieldName());
	}
	

	@Override 
	public Object parse(Object val) {
		if (val == null || val instanceof BigDecimal) {
			return val == null ? null : val;
		} /*else if (val instanceof Integer) {
			return new Long((Integer)val);
		} */ else {
			return new BigDecimal(val.toString());
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + "-amount(" + precision + "," + scale + ")";
	}

	

}