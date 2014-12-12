import xld.node.Node;
import xld.node.ApiHandler;
import xld.node.Controller;


import xld.model.SimpleFlake;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;



public class ApiApp extends Node {

	private SimpleFlake simpleFlake = new SimpleFlake();


	public void start() {
		startModule("api");
		
		info("-----api started------");
		eb().registerHandler("xld-getid", new ApiHandler() {
			public void handle() {
				
				int idCount = getMessage().body().getInteger("idCount");
				JsonArray jids = new JsonArray();
				for (int i = 0; i < idCount; i++) {
					long id = simpleFlake.generate();
					jids.addNumber(id);
				}
				JsonObject r = new JsonObject();
				r.putArray("ids", jids);
				reply(r);
			}
		});
			
		
		
		

	}
}