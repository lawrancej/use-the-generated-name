package language;

import java.util.HashMap;
import java.util.Map;

// Language node representing grouping
public class Group extends Operator<Node> {
	public static final Map<Node, Group> instances = new HashMap<Node, Group>();

	private Group(Node node) {
		child = node;
	}
	public static Node getInstance(Node node) {
		if (! instances.containsKey(node)) {
			instances.put(node, new Group(node));
		}
		return instances.get(node);
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}

}
