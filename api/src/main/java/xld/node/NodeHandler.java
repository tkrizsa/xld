package xld.node;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.eventbus.Message;


/*
	This class works as topmost ApiHandler.
	All the remaining ApiHandlers holds the original 'message' of request, to allow reply and replyError on every level of asynch message-processing.
	This instance shouldn't hold any message info, because one instance exists for each controller function during the whole program running
*/
public abstract class NodeHandler implements Handler<Message<JsonObject>> {
	
	public abstract void handle(final ApiHandler apiHandler);
	
	public void handle(Message<JsonObject> message) {
		ApiHandler a = new ApiHandler() {
			public void handle() {
				/* nothing to do, this instance is only for being parent of further ApiHandler instances! */
			}
		};
		a.setMessage(message);
		handle(a);
	}
	

}