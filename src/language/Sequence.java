package language;

import java.util.HashMap;
import java.util.Map;

// Language node representing a followed by b
public class Sequence extends Operator<Pair<Node,Node>> {

	private static Map<Pair<Node, Node>, Sequence> instances = new HashMap<Pair<Node,Node>, Sequence>();
	private Sequence(Pair<Node, Node> children) {
		this.child = children;
	}
	public static Node getInstance(Node left, Node right) {
		Pair<Node, Node> children = new Pair<Node, Node>(left, right);
		if (! instances.containsKey(children)) {
			instances.put(children, new Sequence(children));
		}
		return instances.get(children);
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
