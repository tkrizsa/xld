package xld.article;


import xld.model.Model;
import xld.node.Node;




public class Article extends Model {

	public static String MODEL_ID = "article";
	
	public Article(Node node) {
		super(node);
		
		setTableName("article.article");
		
		fieldAddId("articleId");
		fieldAddStringProp("articleName", 100);
		fieldAddStringProp("articleDescription", 65536);
	}
	
	
	
}