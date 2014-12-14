package xld.node;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.eventbus.Message;




public abstract class ApiHandler implements Handler<Message<JsonObject>> {

	private ApiHandler parent = null;
	private ApiHandler topMost = null;
	private Message<JsonObject> message;
	private JsonObject response;
	
	
	public ApiHandler() {
		this(null);
	}
	
	public ApiHandler(ApiHandler parent) {
		if (parent == null) {
			this.topMost = this;
		} else {
			this.parent = parent;
			this.topMost = parent.getTopMost();
		}
		ApiHandler tm = getTopMost();
		if (tm.response == null) {
			response = new JsonObject();
			body("");
			status(200);
			contentType("text/plain");		
		}
	}
	
	public ApiHandler getTopMost() {
		if (parent != null) {
			return parent.getTopMost();
		} else {
			return this;
		}
	}
	
	public abstract void handle();
	
	// only topmost comes here, as (before) handle API request
	public void handle(Message<JsonObject> message) {
		this.message = message;
		handle();
	}
	
	// handling topmost request and replies
	public Message<JsonObject> getMessage() {
		return message;
	}
	
	public Message<JsonObject> getTopMostMessage() {
		return topMost.message;
	}
	
	public String getParam(String paramName) {
		JsonObject params = getTopMostMessage().body().getObject("params");
		if (params == null)
			return null;
		else
			return params.getString(paramName);	
	}
	
	
	public void body(String body) {
		if (topMost == null) {
			System.err.println("topMost is null");
		} else if (topMost.response == null) {
			System.err.println("topMost.response is null");
		}
		topMost.response.putString("body", body);
	}
	
	public void status(int status) {
		topMost.response.putNumber("status", status);
	}
	
	public void contentType(String contentType) {
		topMost.response.putString("contentType", contentType);
	}
	
	public void reply() {
		topMost.message.reply(topMost.response);
	}
	
	public void reply(JsonObject rep) {
		topMost.message.reply(rep);
	}
	
	public void replyError(String msg) {
		body(msg);
		contentType("text/plain");
		status(400);
		reply();	
	}
	
	/* -------------------------- sql -------------------------*/
	
	public String sqlStatus() {
		return this.message.body().getString("status");
	}
	
	public boolean sqlOk() {
		return "ok".equals(sqlStatus());
	}

	public boolean sqlError() {
		return !("ok".equals(sqlStatus()));
	}

	public String sqlMessage() {
		return this.message.body().getString("message");
	}
	
	public JsonObject sqlBody() {
		return this.message.body();
	}

}