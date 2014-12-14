package xld.node;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.platform.Verticle;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.buffer.Buffer;
import java.util.Random;
import java.util.Properties;


public class Node extends Verticle {

	String moduleId;

	public void startModule(String moduleId) {
		this.moduleId = moduleId;
		info("XLD " + moduleId + " starting...");	

		EventBus eb = vertx.eventBus();
		
		String address = "/module/" + this.moduleId;
		
		JsonObject reg = new JsonObject();
		reg.putString("kind", "site");
		reg.putString("pattern", "/module/" + this.moduleId);
		reg.putString("address", address);
		
		eb.publish("xld-register-http", reg);
		eb.registerHandler(address, new ModuleRegister());
		
	}
	
	public String getModuleId() {
		return moduleId;
	}
	
	public EventBus eb() {
		return this.vertx.eventBus();
	}
	
	public FileSystem fileSystem() {
		return this.vertx.fileSystem();
	}

	public void info(Object o) {
		container.logger().info(o.toString());
	}
	public void error(Object o) {
		container.logger().error(o.toString());
	}

	public void registerApi(String pattern, final ApiHandler apiHandler) {		
		registerApi(pattern, "get", apiHandler);
	}
	
	public void registerApi(String pattern, String method, final ApiHandler apiHandler) {		
		EventBus eb = vertx.eventBus();
		Random rand = new Random();
		int randomNum = rand.nextInt(9999999) + 10000000;
		String address = pattern + "_" + Integer.toString(randomNum);
		
		JsonObject obj = new JsonObject();
		obj.putString("kind", 		"api");
		obj.putString("pattern", 	pattern);
		obj.putString("address", 	address);
		obj.putString("method", 	method);
		eb.publish("xld-register-http", obj);
		eb.registerHandler(address, apiHandler);
	}

	
	public void registerTemplate(String templatePattern) {
		registerTemplate(templatePattern, templatePattern, templatePattern);
	}
	
	public void registerTemplate(String templatePattern, String indexPattern) {
		registerTemplate(templatePattern, indexPattern, templatePattern);
	}
	
	public void registerTemplate(String templatePattern, String indexPattern, final String fileName) {
		if (moduleId == null || "".equals(moduleId)) {
			error("No module id set!");
			return;
		}
		
		EventBus eb = vertx.eventBus();
		Random rand = new Random();
		int randomNum = rand.nextInt(9999999) + 10000000;
		String address = "template_" + templatePattern + "_" + Integer.toString(randomNum);
		
		
		JsonObject obj = new JsonObject();
		obj.putString("module", 		this.moduleId);
		obj.putString("kind", 			"template");
		obj.putString("pattern", 		"/templates/" + templatePattern);
		obj.putString("indexPattern", 	"/" + indexPattern);
		obj.putString("address", 		address);
		eb.publish("xld-register-http", obj);
		
		eb.registerHandler(address, new Handler<Message<JsonObject>>() {
			public void handle(final Message<JsonObject> message) {
				vertx.fileSystem().readFile("client/"+fileName +".html", new AsyncResultHandler<Buffer>() {
					public void handle(AsyncResult<Buffer> ar) {
						if (ar.succeeded()) {
							info("File contains: " + ar.result().length() + " bytes");
							info(ar.result().toString());
							
							JsonObject res = new JsonObject();
							res.putString("body", ar.result().toString());
							res.putString("contentType", "text/html");
							res.putString("status", "200");
							message.reply(res);
							
						} else {
							error("Failed to read '" + fileName + "' : " + ar.cause());
						}
						
					}
				});				
			
			}
		});
	
	}
	
	// ====================================================== MODULE REGISTER ====================================================
	
	class ModuleRegister extends ApiHandler {
	
		int filesLeft;
		JsonArray modResult = new JsonArray();
		
		
		class FileReader implements AsyncResultHandler<Buffer> {
			String fileName;
			public FileReader(String fileName) {
				this.fileName = fileName;
			}
			
			public void handle(AsyncResult<Buffer> ar) {
				filesLeft--;
				if (ar.succeeded()) {
					info("----------------------" + fileName + "-----------------------");

					/* Extract base name and extension from filename */
					String extension = "";
					String baseName = "";

					int xi = fileName.lastIndexOf('.');
					int xp = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

					if (xi > xp) {
						extension = fileName.substring(xi + 1);
					}			
					baseName = fileName.substring(xp + 1, xi);
					info(baseName);
					info(extension);

					/* Extract properties from file */
					String fileBody = ar.result().toString();
					Properties props = new Properties();
					String lines[] = fileBody.split("\\r?\\n");
					
					for (int i = 0; i < lines.length; i++) {
						String line = lines[i];
						if (!line.startsWith("//@xld"))
							continue;
						String keyval = line.substring(3);
						int p = keyval.indexOf(":");
						String key;
						String val;
						if (p>=0) {
							key = keyval.substring(0, p).trim();
							val = keyval.substring(p+1).trim();
						} else {
							key = keyval.trim();
							val = "true";
						}
						props.setProperty(key, val);
					}
					
					/* Create a Json object for the file */
					JsonObject fileObj = new JsonObject();
					if ("html".equals(extension)) {
						fileObj.putString("kind", "template");
						fileObj.putString("templateName", "/templates/" + baseName);
						fileObj.putString("module", moduleId);
						fileObj.putString("body", fileBody);
						modResult.addObject(fileObj);
					} else if ("js".equals(extension) && "true".equals(props.getProperty("xld-parser"))) {
						fileObj.putString("kind", "parser");
						fileObj.putString("module", moduleId);
						fileObj.putString("body", fileBody);
						modResult.addObject(fileObj);
					} else if ("js".equals(extension) && props.getProperty("xld-controller") != null) {
						fileObj.putString("kind", "parser");
						fileObj.putString("module", moduleId);
						fileObj.putString("name", props.getProperty("xld-controller"));
						fileObj.putString("body", fileBody);
						modResult.addObject(fileObj);
					}
					
				} else {
					error("READ FILE FAILED");
				}
				if (filesLeft <= 0) {
					body(modResult.toString());
					reply();

					
				}
			}
			
			
		}
	
	
		public void handle() {
			vertx.fileSystem().readDir("./client", new AsyncResultHandler<String[]>() {
				public void handle(AsyncResult<String[]> ar) {
					if (ar.succeeded()) {
						filesLeft = ar.result().length;
						for (int i = 0; i < ar.result().length; i++) {
							vertx.fileSystem().readFile(ar.result()[i], new FileReader(ar.result()[i]));
						}
						
					} else {
						error("Failed to read" + ar.cause());
					}
				}
			});			
		}
	}
	

}