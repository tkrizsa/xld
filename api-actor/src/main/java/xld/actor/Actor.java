package xld.actor;


import xld.model.Model;
import xld.node.Node;




public class Actor extends Model {

	public static String MODEL_ID = "actor";
	
	public Actor(Node node) {
		super(node);
		
		setTableName("actor.actor");
		
		fieldAddId("actorId");
		fieldAddStringProp("actorName", 200);
	}
	
	
	
}