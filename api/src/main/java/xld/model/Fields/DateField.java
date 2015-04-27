package xld.model.fields;

import xld.model.ModelBase;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DateField extends Field {
	
	
	public DateField(ModelBase model, String fieldName) {
		super(model, fieldName);
	}
	
	@Override 
	public Field getClone(ModelBase model) {
		return new DateField(model, getFieldName());
	}
	

	@Override
	public Date parse(Object val){
		if (val == null || val instanceof Date) {
			return val == null ? null : (Date)val;
		} else {
			System.out.println("-------------- JSON DATE: ---------------------");
			System.out.println(val.getClass().getName());
			System.out.println(val);
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			try {
				return sdf1.parse(val.toString());
			} catch (ParseException ex) {
				model.getNode().error("Error parsing date from sql " + val.toString());
				return null;
			}
		}
	}
	
	
	@Override
	public void addToJson(ModelBase.Row row, JsonObject jrow) {
		Date val = parse(row.get(fieldName));
		if (val == null) {
			jrow.putValue(fieldName, null);
			return;
		}
		
		
		//TimeZone tz = TimeZone.getTimeZone("UTC");
		//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		//df.setTimeZone(tz);
		String nowAsISO = df.format(val);
		jrow.putString(fieldName, nowAsISO);
	}
	
	@Override
	public void addToJson(ModelBase.Row row, JsonArray jrow) {
		Date val = parse(row.get(fieldName));
		if (val == null) {
			jrow.add(null);
			return;
		}
		
		
		//TimeZone tz = TimeZone.getTimeZone("UTC");
		//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		//df.setTimeZone(tz);
		String nowAsISO = df.format(val);
		jrow.addString(nowAsISO);
	}
	
	
	@Override
	public String toString() {
		return super.toString() + "-date";
	}


}
