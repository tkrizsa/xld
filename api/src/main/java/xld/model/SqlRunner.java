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
	
	public SqlRunner(Node node) {
		this.node = node;
		
	}
	

	public void insert(Model model, Model.Row row) {
		
		JsonObject q = new JsonObject();
		q.putString("action", "insert");
		q.putString("table", model.getTableName());
		
		JsonArray jfields = new JsonArray();
		JsonArray jrows = new JsonArray();
		JsonArray jvalues = new JsonArray();
		for (Field field : model.getFields()) {
			jfields.addString(field.getFieldName());
			field.addToJson(row, jvalues);
		}
		q.putArray("fields", jfields);
		jrows.addArray(jvalues);
		q.putArray("values", jrows);
		
		queue.add(q);
	}
	
	public void update(Model model, Model.Row row) {
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
						replyError(sqlMessage());
						return;
					}
					thisRunner.run(apiHandler);
				}
			});
		
		}
	
	
	}



}