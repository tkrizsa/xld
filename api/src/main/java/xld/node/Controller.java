package xld.node;

import xld.model.Model;
import java.lang.reflect.Field;
import org.vertx.java.core.json.JsonObject;

public  class Controller {

	protected Node node;
	protected Class<? extends Model> modelClass;

	public Controller(Node node, Class<? extends Model> modelClass) {
		this.node = node;
		this.modelClass = modelClass;
		
		// try {
			// Model m;
			// info("==================== itt ====================");
			// m = mt.getConstructor(Node.class).newInstance(this);
			// Article aa = new Article(this);
			// info(m.getModelIdPlural());
			// info(aa.getModelIdPlural());
			// info("==================== ott ====================");
			
			/*Method mm = mt.getDeclaredMethod("getModelId" );
			mm.setAccessible(true); //if security settings allow this
			Object o = mm.invoke(null, 23); //use null if the method is static*/
			
			// Field f = mt.getField("MODEL_ID");
			// info(f.get(null));
			
		// } catch (Exception ex) {
			// error("==== error ====");
			// error(ex.getMessage());
		// }
		
		
		
	}
	

	public Model createModel() {
		Model model = null;
		try {
			model = modelClass.getConstructor(Node.class).newInstance(node);
		} catch (Exception ex) {
			node.error("==== createModel error ====");
			node.error(ex.getMessage());
		}
		return model;
	}
	
	public String getModelId() {
		String modelId = "yyy";
		try {
			Field f = modelClass.getField("MODEL_ID");
			modelId = f.get(null).toString();
		} catch (Exception ex) {
			node.error("==== error ====");
			node.error(ex.getMessage());
		}
		return modelId;
	}

	public String getModelIdPlural() {
		String modelId = "yyys";
		try {
			Field f = modelClass.getField("MODEL_ID");
			modelId = f.get(null).toString();
		} catch (Exception ex) {
			node.error("==== error ====");
			node.error(ex.getMessage());
		}
		return modelId + "s";
	}

	public void publish() {
		installModel();
		publishList();
		publishItem();
		node.registerTemplate(getModelIdPlural());
		node.registerTemplate(getModelId(), getModelIdPlural() + "/:id");		
	}
	
	public void installModel() {
		createModel().install();
	}
	
	public void publishList() {
	
		node.registerApi("/api/" + getModelIdPlural() , new NodeHandler () {
			public void handle(final ApiHandler apiHandler) {
			
				final Model a = createModel();
				a.sqlLoadList(new ApiHandler(apiHandler) {
					public void handle() {
						try {
							body(a.jsonGet());
							contentType("application/json");
							reply();	
						} catch (Exception ex) {
							replyError(ex.getMessage());
						}
					}
				});
			}
		});
	
	
	}
	
	
	public void publishItem() {
	
		node.registerApi("/api/" + getModelIdPlural() + "/:id" , new NodeHandler () {
			public void handle(final ApiHandler apiHandler) {
			
				final Model a = createModel();
				String keys = apiHandler.getMessage().body().getObject("params").getString("id");
				
				if ("new".equals(keys)) {
					a.rowAdd();
					apiHandler.body(a.jsonGet());
					apiHandler.contentType("application/json");
					node.info("*** reply statuscode : " + apiHandler.getStatus());
					apiHandler.reply();
					return;
				}
				
				a.sqlLoadByKeys(keys, new ApiHandler(apiHandler) {
					public void handle() {
						if (a.empty()) {
							replyError("Not exists");
							return;
						}
						body(a.jsonGet());
						contentType("application/json");
						node.info("*** reply statuscode : " + apiHandler.getStatus());
						reply();
						return;
					}
				});
			}
		});
	
		node.registerApi("/api/" + getModelIdPlural() + "/:id" , "post", new NodeHandler () {
			public void handle(final ApiHandler apiHandler) {
			
				final Model a = createModel();
				String keys = apiHandler.getParam("id");
				
				String s =  apiHandler.getMessage().body().getString("body");
				a.loadFromJson(new JsonObject(s));
				
				a.sqlSave(new ApiHandler(apiHandler) {
					public void handle() {
						body(a.jsonGet());
						contentType("application/json");
						reply();
					}
				});
			}
		});
	
	
	}
	
	
	
	

}