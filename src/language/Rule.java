package language;

import java.util.HashMap;
import java.util.Map;

// Equals operator in a language
public class Rule extends Operator<Pair<Identifier,Node>> {
	private static Map<Identifier, Rule> instances = new HashMap<Identifier, Rule>();
	private Rule(Pair<Identifier, Node> children) {
		this.child = children;
	}
	
	// Create rule for the identifier
	public static Node create(Identifier id, Node node) {
		Pair<Identifier, Node> children = new Pair<Identifier, Node>(id, node);
		instances.put(id, new Rule(children));
		return instances.get(id);
	}
	
	// Get rule for the identifier
	public static Rule getInstance(Identifier id) {
		return instances.get(id);
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
