package xld.model;

import xld.node.Node;

public class Model extends ModelInstallable {

	public Model(Node node) {
		super(node);
	}

	public Model(Node node, ModelBase parent) {
		super(node, parent);
	}

}
