package xld.article;


import xld.model.Model;
import xld.model.ModelBase;
import xld.node.Node;




public class Article extends Model {

	public static String MODEL_ID = "article";
	
	public Article(Node node) {
		this(node, null);
	}
	
	public Article(Node node, ModelBase parent) {
		super(node, parent);
		
		setTableName("article.article");
		
		fieldAddId("articleId");
		fieldAddStringProp("articleName", 100);
		fieldAddStringProp("articleDescription", 65536);
	}
	
	
	
}