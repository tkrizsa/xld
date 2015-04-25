package xld.model.fields;

import xld.model.ModelBase;
import java.math.BigDecimal;
import java.math.MathContext;

public class MoneyField extends Field {
	
	
	public MoneyField(ModelBase model, String fieldName) {
		super(model, fieldName);
	}
	
	@Override 
	public Field getClone(ModelBase model) {
		return new MoneyField(model, getFieldName());
	}
	

	@Override
	public Object parse(Object val){
		if (val == null || val instanceof BigDecimal) {
			return val == null ? null : val;
		} else if (val instanceof scala.math.BigDecimal) {
			return ((scala.math.BigDecimal)val).bigDecimal();
		} else if (val instanceof Integer) {
			return new BigDecimal(val.toString());
		} else if (val instanceof Double) {
			return new BigDecimal((Double)val, MathContext.DECIMAL64);
		} else {
			//return Long.parseLong(val.toString());
			//throw new IllegalFieldDataException();
			System.out.println("------------------ NO MONEY ---------------------");
			System.out.println(val.getClass().getName());
			return null;
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + "-money";
	}


}
