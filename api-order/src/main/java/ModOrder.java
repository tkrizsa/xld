import xld.node.Node;
import xld.node.ApiHandler;
import xld.node.Controller;


import xld.order.Order;
import xld.order.Detail;


public class ModOrder extends Node {

	public void start() {
		startModule("order");
		
		new Controller(this, Order.class).publish();
		new Controller(this, Detail.class).publish();

	}
}