package xld.actor;


import xld.model.Model;
import xld.model.ModelBase;
import xld.node.Node;




public class Actor extends Model {

	public static String MODEL_ID = "actor";
	
	public Actor(Node node) {
		this(node, null);
	}
	
	public Actor(Node node, ModelBase parent) {
		super(node, parent);
		
		setTableName("actor.actor");
		
		fieldAddId("actorId");
		fieldAddStringProp("actorName", 200);
	}
	
	
	
}