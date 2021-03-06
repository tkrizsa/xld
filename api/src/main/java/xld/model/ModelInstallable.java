package xld.model;

import xld.node.Node;
import xld.node.ApiHandler;
import xld.model.fields.Field;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;
import java.util.Iterator;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.buffer.Buffer;
import java.util.List;
import java.util.ArrayList;


public class ModelInstallable extends ModelBase {

	public ModelInstallable(Node node) {
		super(node);
	}
	
	public ModelInstallable(Node node, ModelBase parent) {
		super(node, parent);
	}
	
	public void install() {
		node.info("INSTALLing... " + getTableName());
		
		final ModelInstallable thisModel = this;

		/* 
			Calculate current md5, using tablename, versionString and fields info 
			Result is an MD5 string, stored in 'vst' variable
		*/
		StringBuilder vstBase = new StringBuilder();
		vstBase.append(getTableName() + "#" + getVersionString() + "#");
		for(Field field : fields) {
			vstBase.append(field.toString() + "|");
		}
		node.info(vstBase.toString());
		
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			node.error(ex.getMessage());
			return;
		}
		final String vst = ((new HexBinaryAdapter()).marshal(md5.digest(vstBase.toString().getBytes()))).toLowerCase();
		
		node.info(vst);
		
		/* 
			From database, read content of xld_sql_install table, to compare with current version 
		*/
		final SqlRunner sql = new SqlRunner(node);
		sql.prepared("SELECT modelVersion FROM xld_sql_install WHERE modelId = ? ORDER BY installId ASC", 
			new Object[] {node.getModuleId() + "." + getModelId()}
		);
	
		sql.run(new ApiHandler() {
			public void handle() {
				/*
					Get the last installed version from database
				*/
				String curVer = "";
				final JsonArray jresults = sql.getLastResult().getArray("results");
				Iterator<Object> jresultIt = jresults.iterator();
				while (jresultIt.hasNext()) {
					JsonArray jrow = (JsonArray)jresultIt.next();
					curVer = (String)jrow.get(0);
				}
				/*
					If current version equals last installed version,
					nothing to do, return
				*/
				if (vst.equals(curVer)) {
					node.info("database up-to-date.");
					return;
				}
				final String finalCurVer = curVer;
				/*
					In other case we must load (asynchronously) the install scripts from 'install' resource folder 
				*/
				thisModel.loadInstallScripts(new ApiHandler(this) {
					public void handle() {
						/* 
							find the last install script index we should install (where modelVersion equals current version) 
						*/
						int inAct = -1; 
						for (int i = installScripts.size()-1; i>=0; i--) {
							if (vst.equals(installScripts.get(i).modelVersion)) {
								inAct = i;
								break;
							}
						}
						if (inAct < 0) {
							node.error("INSTALL REQUIRED, BUT MISSING!\r\nmodel: " + thisModel.getModelId() + "\r\nversion : " + vst);
							return;
						}
						/*
							find the first install script index, that's not installed this sql server yet 
						*/
						int inLast = -2;
						if ("".equals(finalCurVer)) {
							/* this is the first install, start scripts from beginning */
							inLast = -1;
						} else {
							for (int i = installScripts.size()-1; i>=0; i--) {
								if (finalCurVer.equals(installScripts.get(i).modelVersion)) {
									inLast = i;
									break;
								}
							}
						}
						if (inLast < -1) {
							node.error("INSTALL REQUIRED, BUT LAST INSTALLED VERSION MISSING!\r\nmodel: " + thisModel.getModelId() + "\r\nversion : " + finalCurVer);
							return;
						}
						
						/*
							run all the scripts between first and last
						*/
						node.info("current : " + inAct + "; last : " + inLast);
						SqlRunner sql = new SqlRunner(node);
						for (int i = inLast+1; i<=inAct; i++) {
							InstallScript script = installScripts.get(i);

							StringBuilder fullScript = new StringBuilder();
							
							node.info("INSTALLING VERSION : " + script.modelVersion);

							
							for (String statement : script.statements) {
								sql.prepared(statement);
								fullScript.append(statement + "\r\n\r\n");
							}
							
							String query = "INSERT INTO xld_sql_install (modelId, modelVersion, versionHint, sqlScript) VALUES (?,?,?,?)";
							sql.prepared(query, new Object[] {
								node.getModuleId() + "." + getModelId(),
								script.modelVersion,
								script.versionHint,
								fullScript.toString()
							});
							
						}
						
						sql.run(new ApiHandler(this) {
							public void handle() {
								node.info("INSTALL OK.");
							}
						});
					}
				});
			}
		});
	}
	
	private static class InstallScript {
		public String modelVersion;
		public String versionHint;
		public List<String> statements;
		
		public InstallScript(String modelVersion, String versionHint, List<String> statements) {
			this.modelVersion = modelVersion;
			this.versionHint  = versionHint;
			this.statements    = statements;
		
		
		}
	
	}
	
	private List<InstallScript> installScripts;

	/* 
		loads the client/[modelId].sql install script file and loads it in installScripts array
		the file contains more scrips, one for each version step
	*/
	private void loadInstallScripts(final ApiHandler apiHandler) {
		final String fileName = "./install/" + getModelId() + ".sql";
		node.fileSystem().readFile(fileName, new AsyncResultHandler<Buffer>() {
			public void handle(AsyncResult<Buffer> ar) {
				if (!ar.succeeded()) {
					node.error("file not found: '"  + fileName + "'");
					//apiHandler.handle();
					return;
				}
				
				installScripts = new ArrayList<InstallScript>();
			
			
				String fileBody = ar.result().toString();
				node.info("------------------- sql install file read : ---------------------");				
				
				String modelVersion = null;
				String versionHint = null;
				List<String> statements = new ArrayList<String>();
				String statement = "";
				
				String lines[] = fileBody.split("\\r?\\n");
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i].trim();
					if (line.startsWith("--@xld-")) {

						String keyval = line.substring(7);
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
						
						if ("go".equals(key)) {
							statements.add(statement);
							statement = "";
							continue;
						}
					
						if (!("".equals(statement)) || statements.size()>0) {
							if (!("".equals(statement))) {
								statements.add(statement);
								statement = "";
							}
							installScripts.add(new InstallScript(modelVersion, versionHint, statements));
							modelVersion = null;
							versionHint = null;
							statement = "";
							statements = new ArrayList<String>();
						}
					
						if ("modelVersion".equals(key)) {
							modelVersion = val;
						} else if ("versionHint".equals(key)) {
							versionHint = val;
						} else {
							node.error("Unknown property in install script file : '" + key + "' at line " + (i+1));
						}
					} else if (!("".equals(line))) {
						statement += line + "\r\n";
					}
				}
				if (!("".equals(statement)) || statements.size()>0) {
					if (!("".equals(statement))) 
						statements.add(statement);
					installScripts.add(new InstallScript(modelVersion, versionHint, statements));
				}

				for (InstallScript s : installScripts) {
					node.info("MODELLVERSION : " + s.modelVersion);
					node.info("VERSIONHINT : " + s.versionHint);
					node.info("STATEMENTS ("+ s.statements.size() +") : ");
					for (String st : s.statements) {
						node.info("ST:");
						node.info(st);
					}
				}
				
				apiHandler.handle();
			
			}
		
		});		
	
	
	}

}
