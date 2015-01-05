package xld.model.fields;

import xld.model.ModelBase;


/*
	Special field for holding reference to external table, use snowflake keys
	Behave like id in json : convert values to string, not to loss precision

*/
public class ReferenceField extends LongField {
	
	Class<? extends ModelBase> referenceModel;
	
	public ReferenceField(ModelBase model, String fieldName,  Class<? extends ModelBase> referenceModel) {
		super(model, fieldName);
		this.referenceModel = referenceModel;
	}
	
	@Override 
	public ReferenceField getClone(ModelBase model) {
		return new ReferenceField(model, getFieldName(), referenceModel);
	}
	
	
	@Override
	public String toString() {
		return super.toString() + "-reference-to-" + referenceModel.getName();
	}

	
	

}
