package xld.homebudget;


import xld.model.Model;
import xld.model.ModelBase;
import xld.node.Node;

import xld.homebudget.Expense;
import xld.actor.Actor;



public class Expense extends Model {

	public static String MODEL_ID = "expense";
	
	public Expense(Node node) {
		this(node, null);
	}
	
	public Expense(Node node, ModelBase parent) {
		super(node, parent);
		
		setTableName("homebudget.expense");
		setVersionString("**");
		
		fieldAddId("expenseId");
		fieldAddReference("expenseKindId", ExpenseKind.class, "expenseKind");
		fieldAddReference("actorId", Actor.class, "actor");
		fieldAddMoney("amount");
		fieldAddDate("date");
		fieldAddStringProp("expenseDescription", 65536);
		
		


	}
}