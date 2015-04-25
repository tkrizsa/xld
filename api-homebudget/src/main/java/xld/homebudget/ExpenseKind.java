package xld.homebudget;


import xld.model.Model;
import xld.model.ModelBase;
import xld.node.Node;

public class ExpenseKind extends Model {

	public static String MODEL_ID = "expensekind";
	
	public ExpenseKind(Node node) {
		this(node, null);
	}
	
	public ExpenseKind(Node node, ModelBase parent) {
		super(node, parent);
		
		setTableName("homebudget.expensekind");
		
		fieldAddId("expenseKindId");
		fieldAddStringProp("expenseName", 200);
	}
	
	
	
}