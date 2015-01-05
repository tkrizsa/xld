package xld.order;


import xld.model.Model;
import xld.model.ModelBase;
import xld.node.Node;

import xld.actor.Actor;



public class Order extends Model {

	public static String MODEL_ID = "order";
	
	public Order(Node node) {
		this(node, null);
	}
	
	public Order(Node node, ModelBase parent) {
		super(node, parent);
		
		setTableName("order.order");
		
		fieldAddId("orderId");
		fieldAddStringProp("orderName", 100);
		fieldAddStringProp("orderDescription", 65536);
		
		fieldAddReference("deliveryActorId", Actor.class, "deliveryActor");
		fieldAddReference("invoiceActorId", Actor.class,  "invoiceActor");
		
		expandAddDetail("details", Detail.class, "orderId");


	}
	
	
	
}