package xld.order;


import xld.model.Model;
import xld.node.Node;

import xld.actor.Actor;



public class Order extends Model {

	public static String MODEL_ID = "order";
	
	public Order(Node node) {
		super(node);
		
		setTableName("order.order");
		
		fieldAddId("orderId");
		fieldAddStringProp("orderName", 100);
		fieldAddStringProp("orderDescription", 65536);
		fieldAddReference("actorId", Actor.class);
	}
	
	
	
}