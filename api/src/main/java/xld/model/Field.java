package xld.model;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;



public abstract class Field {

	protected String fieldName;
	protected ModelBase model;
	private boolean sqlField = true;
	private Field masterView; 				// points to a cloned field in a master model, in case of view only reference query
	private Model.Expand expand;			// if field can be expanded, this refers to owner model's Expand object
	
	
	public Field(ModelBase model, String fieldName) {
		this.model = model;
		this.fieldName = fieldName;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public boolean isPrimaryKey() {
		return false;
	}

	public boolean isPrimaryKeyLive(ModelBase.Row row) {
		return false;
	}
	
	public void setSqlField(boolean value) {
		sqlField = false;
	}
	
	public boolean getSqlField() {
		return sqlField;
	}

	public abstract void addToJson(ModelBase.Row row, JsonObject jrow);
	public abstract void addToJson(ModelBase.Row row, JsonArray jrow);
	public abstract void getFromJson(ModelBase.Row row, JsonObject jrow);
	public abstract void getFromJson(ModelBase.Row row, JsonArray jrow, int ix);
	
	public abstract Object parse(Object val);

	public void set(ModelBase.Row row, Object val) {
		row.set(fieldName, parse(val));
	}
	
	public Object get(ModelBase.Row row) {
		return row.get(fieldName);
	}
	
	@Override
	public String toString() {
		return getFieldName();
	}
	
	public abstract Field getClone(ModelBase model);
		
	public Field cloneToMasterView(ModelBase master) {
		Field cf = this.getClone(master);
		cf.setSqlField(false);
		this.masterView = cf;
		master.getFields().add(cf);
		return cf;
	}
	
	public Field getMasterView() {
		return masterView;
	}
	
	public void setExpand(Model.Expand expand) {
		this.expand = expand;
	}
	
	public Model.Expand getExpand() {
		return this.expand;
	}
	
	public boolean isFieldExpanded() {
		Model.Expand e = getExpand();
		return (e!=null && !e.viewOnly);
	}
	
	
	
}

