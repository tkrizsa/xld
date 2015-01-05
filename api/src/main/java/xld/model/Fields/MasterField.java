package xld.model.fields;

import xld.model.ModelBase;




public class MasterField extends ReferenceField {

	public MasterField(ModelBase model, String fieldName,  Class<? extends ModelBase> referenceModel) {
		super(model, fieldName, referenceModel);
	}
	
	@Override 
	public MasterField getClone(ModelBase model) {
		return new MasterField(model, getFieldName(), referenceModel);
	}
	
	@Override
	public String toString() {
		return super.toString() + "-master";
	}


}

