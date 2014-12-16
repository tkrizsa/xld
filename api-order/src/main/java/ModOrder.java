import xld.node.Node;
import xld.node.ApiHandler;
import xld.node.Controller;


import xld.order.Order;


public class ModOrder extends Node {

	public void start() {
		startModule("order");
		
		new Controller(this, Order.class).publish();

	}
}