package xld.model.fields;

import xld.model.ModelBase;
import java.math.BigDecimal;
import java.math.MathContext;
import org.vertx.java.core.json.JsonObject;


public class MoneyField extends Field {
	
	
	public MoneyField(ModelBase model, String fieldName) {
		super(model, fieldName);
	}
	
	@Override 
	public Field getClone(ModelBase model) {
		return new MoneyField(model, getFieldName());
	}
	

	@Override
	public java.math.BigDecimal parse(Object val){
		if (val == null || val instanceof BigDecimal) {
			return val == null ? null : (BigDecimal)val;
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
			System.out.println(scala.math.BigDecimal.class.getName());
			return null;
		}
	}
	
	@Override
	public void addToJson(ModelBase.Row row, JsonObject jrow) {
		java.math.BigDecimal val = parse(row.get(fieldName));
		jrow.putNumber(fieldName, val);
	}
	
	
	@Override
	public String toString() {
		return super.toString() + "-money";
	}


}
