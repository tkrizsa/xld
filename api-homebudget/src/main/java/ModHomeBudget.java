import xld.node.Node;
import xld.node.ApiHandler;
import xld.node.Controller;


import xld.homebudget.ExpenseKind;
import xld.homebudget.Expense;


public class ModHomeBudget extends Node {

	public void start() {
		startModule("homebudget");
		
		new Controller(this, ExpenseKind.class).publish();
		new Controller(this, Expense.class).publish();

	}
}