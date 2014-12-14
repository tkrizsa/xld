package xld.model;

import xld.node.Node;
import xld.node.ApiHandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;
import java.util.Iterator;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.buffer.Buffer;


public class ModelInstallable extends ModelBase {

	public ModelInstallable(Node node) {
		super(node);
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
		final String vst = (new HexBinaryAdapter()).marshal(md5.digest(vstBase.toString().getBytes()));
		
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
				node.info("------------- install select returned... -----------------");
				node.info(sql.getLastResult());
		
				/*
					Get the last installed version from database
				*/
				String curVer = "";
				JsonArray jresults = sql.getLastResult().getArray("results");
				Iterator<Object> jresultIt = jresults.iterator();
				while (jresultIt.hasNext()) {
					JsonArray jrow = (JsonArray)jresultIt.next();
					curVer = (String)jrow.get(0);
				}
				
				/*
					If current version equals last installed version,
					nothing to do, return
				*/
				if (curVer == vst) {
					return;
				}
				/*
					In other case we must load (asynchronously) the install scripts from 'install' resource folder 
				*/
				thisModel.loadInstallScripts(new ApiHandler(this) {
					public void handle() {
					
					
					}
				});
			}
		
		
		});
	
	}
	
	private void loadInstallScripts(final ApiHandler apiHandler) {
		node.fileSystem().readFile("./install/" + getModelId() + ".sql", new AsyncResultHandler<Buffer>() {
			public void handle(AsyncResult<Buffer> ar) {
				if (!ar.succeeded()) {
					node.error("File not found.");
					apiHandler.handle();
					return;
				}
			
			
				String fileBody = ar.result().toString();
				node.info("------------------- sql install file read : ---------------------");
				node.info(fileBody);
				/*
				String lines[] = fileBody.split("\\r?\\n");
				for (int i = 0; i < lines.length; i++) {
				
				}
				*/
			
			}
		
		});		
	
	
	}

}
