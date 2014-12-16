import xld.node.Node;
import xld.node.ApiHandler;
import xld.node.Controller;


import xld.actor.Actor;


public class ModActor extends Node {

	public void start() {
		startModule("actor");
		
		new Controller(this, Actor.class).publish();

	}
}