package xld.model;

import xld.node.Node;
import xld.node.ApiHandler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;

import java.lang.NoSuchMethodException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.lang.Exception;



public class PublishedModel extends Model {

	public PublishedModel(Node node) {
		super(node);
		
	}
/*	
	public static PublishedModel createNew2(Node node) {
		return null;
	}
	
	public static void publish(final Node node) {

	
		node.registerApi("/api/" + getModelIdPlural(), new ApiHandler () {
			public void handle() {
			
				final PublishedModel a = createNew2(node);
				a.sqlLoadList(new ApiHandler(this) {
					public void handle() {
						body(a.jsonGet());
						contentType("application/json");
						reply();	
					}
				});
			}
		});
		
	}
	
	*/
	
	/*public void sqlLoadAndReply(String query, final ApiHandler apiHandler) {
		sqlLoad(query, new AsyncResultHandler<Model>() {
			public void handle(AsyncResult<Model> ar) {
				if (ar.succeeded()) {
					apiHandler.body(jsonGet());
					apiHandler.contentType("application/json");
					apiHandler.reply();	
				} else {
					apiHandler.body(ar.cause().getMessage());
					apiHandler.contentType("text/plain");
					apiHandler.status(400);
					apiHandler.reply();	
				}
			}
		});
	}*/
	

/*	public void publishItem() {
		node.registerApi("/api/" + getModelId() + "/:id", new ApiHandler () {
			public void handle() {
				String keys = message.body().getObject("params").getString("id");
				String query = "SELECT * FROM `" + getTableName() + "` WHERE " + keySqlWhere(keys);
				createNew().sqlLoadAndReply(query, this);
			}
		});
	}
*/
}