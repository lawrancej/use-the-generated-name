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
		if (left == EmptySet.getInstance() || right == EmptySet.getInstance()) {
			return EmptySet.getInstance();
		}
		if (left == EmptyString.getInstance()) {
			return right;
		}
		if (right == EmptyString.getInstance()) {
			return left;
		}
		Pair<Node, Node> children = new Pair<Node, Node>(left, right);
		if (! instances.containsKey(children)) {
			instances.put(children, new Sequence(children));
		}
		return instances.get(children);
	}
	public static Node getInstance(Node[] nodes, int i) {
		if (i >= nodes.length) {
			return EmptyString.getInstance();
		} else {
			return Sequence.getInstance(nodes[i], Sequence.getInstance(nodes, i+1));
		}
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
