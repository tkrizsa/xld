import xld.node.Node;
import xld.node.ApiHandler;
import xld.node.Controller;


import xld.article.Article;


public class ModArticle extends Node {

	public void start() {
		startModule("article");
		
		new Controller(this, Article.class).publish();

	}
}