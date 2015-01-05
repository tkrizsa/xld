package xld.order;


import xld.model.Model;
import xld.model.ModelBase;
import xld.node.Node;

import xld.article.Article;



public class Detail extends Model {

	public static String MODEL_ID = "detail";
	
	public Detail(Node node) {
		this(node, null);
	}
	
	public Detail(Node node, ModelBase parent) {
		super(node, parent);
		
		setTableName("order.detail");
		
		fieldAddId("detailId");
		fieldAddMaster("orderId", Order.class);
		fieldAddReference("articleId", Article.class);
		fieldAddAmountProp("amount");
		

	}
	
	
	
}