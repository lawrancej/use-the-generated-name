package language;

import java.util.HashMap;
import java.util.Map;

public class Nonterminal implements Node {
	private static final Map<String, Nonterminal> instances = new HashMap<String, Nonterminal>();
	public final String label;
	protected Node rule;
	private Nonterminal(String label) {
		this.label = label;
		rule = EmptySet.getInstance();
	}
	public static Nonterminal getInstance(String label) {
		if (! instances.containsKey(label)) {
			instances.put(label, new Nonterminal(label));
		}
		return instances.get(label);
	}
	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
	public void derive(Node... nodes) {
		if (nodes.length == 0) {
			rule = Or.getInstance(rule, EmptyString.getInstance());
		} else if (nodes.length == 1) {
			rule = Or.getInstance(rule, nodes[0]);
		} else {
			Node result = Sequence.getInstance(nodes[0], nodes[1]);
			for (int i = 2; i < nodes.length; i++) {
				result = Sequence.getInstance(result, nodes[i]);
			}
			rule = Or.getInstance(rule, result);
		}
	}
}
