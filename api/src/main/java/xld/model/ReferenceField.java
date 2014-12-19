package xld.model;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;

/*
	Special field for holding reference to external table, use snowflake keys
	Behave like id in json : convert values to string, not to loss precision

*/
public class ReferenceField extends IdField {
	
	 Class<? extends Model> referenceModel;
	
	public ReferenceField(ModelBase model, String fieldName,  Class<? extends Model> referenceModel) {
		super(model, fieldName);
		this.referenceModel = referenceModel;
	}
	
	@Override 
	public ReferenceField getClone(ModelBase model) {
		return new ReferenceField(model, getFieldName(), referenceModel);
	}
	
	
	public boolean isPrimaryKey() {
		return false;
	}
	
	
	@Override
	public String toString() {
		return super.toString() + "-reference-to-" + referenceModel.getName();
	}


}
