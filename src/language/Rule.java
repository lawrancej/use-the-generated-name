package language;

import java.util.HashMap;
import java.util.Map;

public class Rule extends Operator<Pair<Nonterminal,Node>> {
	private static Map<Pair<Nonterminal, Node>, Rule> instances = new HashMap<Pair<Nonterminal,Node>, Rule>();
	private Rule(Pair<Nonterminal, Node> children) {
		this.child = children;
	}
	public static Node getInstance(Nonterminal nonterminal, Node rule) {
		Pair<Nonterminal, Node> children = new Pair<Nonterminal, Node>(nonterminal, rule);
		if (! instances.containsKey(children)) {
			instances.put(children, new Rule(children));
		}
		return instances.get(children);
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
