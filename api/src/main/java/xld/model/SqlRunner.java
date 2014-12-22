package xld.model;


import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;
import xld.node.Node;
import xld.node.ApiHandler;

import java.util.List;
import java.util.ArrayList;


public class SqlRunner {

	protected Node node;
	private List<JsonObject> queue = new ArrayList<JsonObject>();
	private int nextRun = 0;
	private JsonObject lastResult;
	
	public SqlRunner(Node node) {
		this.node = node;
	}
	

	public void insert(ModelBase model, ModelBase.Row row) {
		JsonObject q = new JsonObject();
		q.putString("action", "insert");
		q.putString("table", model.getTableName());
		
		JsonArray jfields = new JsonArray();
		JsonArray jrows = new JsonArray();
		JsonArray jvalues = new JsonArray();
		for (Field field : model.getFields()) {
			jfields.addString(field.getFieldName());
			field.addToJson(row, jvalues);
			/*if (!model.isFieldExpanded(field)) {
				field.addToJson(row, jvalues);
			} else {
				Model.Expand e = field.getExpand();
				field.addToJson(e.
				for (Model.Expand e : model.currExpands) {
				
				}
			}*/
		}
		q.putArray("fields", jfields);
		jrows.addArray(jvalues);
		q.putArray("values", jrows);
		
		queue.add(q);
	}
	
	public void update(ModelBase model, ModelBase.Row row) {
		JsonObject q = new JsonObject();
		q.putString("action", "prepared");
		
		StringBuilder statement = new StringBuilder();
		statement.append("UPDATE `" + model.getTableName() + "` SET ");
		
		
		JsonArray jfields = new JsonArray();
		JsonArray jvalues = new JsonArray();
		boolean first = true;
		for (Field field : model.getFields()) {
			if (!field.isPrimaryKey()) {
				statement.append((first?"":", ") + "`" + field.getFieldName() + "` = ? ");
				field.addToJson(row, jvalues);
				first = false;
			}
		}
		statement.append("\r\nWHERE\r\n");
		first = true;
		for (Field field : model.getFields()) {
			if (field.isPrimaryKey()) {
				statement.append((first?"":" AND ") + "`" + field.getFieldName() + "` = ? ");
				field.addToJson(row, jvalues);
				first = false;
			}
		}
		
		q.putString("statement", statement.toString());
		q.putArray("values", jvalues);
		
		queue.add(q);
	}
	
	
	
	public void prepared(String statement) {
		prepared(statement, new Object[0]);
	}
	
	public void prepared(String statement, Object[] values) {
		JsonObject q = new JsonObject();
		q.putString("action", "prepared");
		q.putString("statement", statement);
		
		JsonArray jvalues = new JsonArray();
		for (int i = 0; i < values.length; i++) {
			jvalues.add(values[i]);
		}
		q.putArray("values", jvalues);
		queue.add(q);
	}


	public void run(final ApiHandler apiHandler) {
		final SqlRunner thisRunner = this;
		if (queue.size() <= nextRun) {
			apiHandler.handle();
		} else {
			JsonObject q = queue.get(nextRun);
			nextRun++;
			node.info(q);
			node.eb().send("xld-sql-persist", q, new ApiHandler(apiHandler) {
				public void handle() {
					if (sqlError()) {
						node.error(sqlMessage());
						replyError(sqlMessage());
						return;
					} 
					thisRunner.lastResult = this.getMessage().body();
					thisRunner.run(apiHandler);
				}
			});
		
		}
	
	
	}
	
	public JsonObject getLastResult() {
		return lastResult;
	}



}