package language;

import java.util.HashMap;
import java.util.Map;

public class Identifier implements Node {
	private static final Map<String, Identifier> instances = new HashMap<String, Identifier>();
	public final String label;
	protected Node rule;
	private Identifier(String label) {
		this.label = label;
		rule = EmptySet.getInstance();
	}
	public static Identifier getInstance(String label) {
		if (! instances.containsKey(label)) {
			instances.put(label, new Identifier(label));
		}
		return instances.get(label);
	}
	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
	public void derive(Node... nodes) {
		if (nodes.length == 0) {
			rule = Or.getInstance(rule, Rule.getInstance(this, EmptyString.getInstance()));
		} else if (nodes.length == 1) {
			rule = Or.getInstance(rule, Rule.getInstance(this, nodes[0]));
		} else {
			Node result = Sequence.getInstance(nodes[0], nodes[1]);
			for (int i = 2; i < nodes.length; i++) {
				result = Sequence.getInstance(result, nodes[i]);
			}
			rule = Or.getInstance(rule, Rule.getInstance(this, result));
		}
	}
}
