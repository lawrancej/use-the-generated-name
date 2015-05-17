package language;

import java.util.HashMap;
import java.util.Map;

// Regex node representing a|b
public class Or extends Operator<Pair<Node,Node>> {
	
	private static Map<Pair<Node, Node>, Or> instances = new HashMap<Pair<Node,Node>, Or>();
	private Or(Pair<Node, Node> children) {
		this.child = children;
	}
	public static Node getInstance(Node left, Node right) {
		Pair<Node, Node> children = new Pair<Node, Node>(left, right);
		if (! instances.containsKey(children)) {
			instances.put(children, new Or(children));
		}
		return instances.get(children);
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}

}
