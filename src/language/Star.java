package language;

import java.util.HashMap;
import java.util.Map;

// Regex node representing a*
public class Star extends Operator<Node> {
	public static final Map<Node, Star> instances = new HashMap<Node, Star>();

	private Star(Node node) {
		child = node;
	}
	public static Node getInstance(Node node) {
		if (! instances.containsKey(node)) {
			instances.put(node, new Star(node));
		}
		return instances.get(node);
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
