package language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Rule extends Operator<Pair<Identifier,Node>> {
	private static Map<Pair<Identifier, Node>, Rule> instances = new HashMap<Pair<Identifier,Node>, Rule>();
	private static Map<Identifier, ArrayList<Rule>> instanceMap = new HashMap<Identifier, ArrayList<Rule>>();
	private Rule(Pair<Identifier, Node> children) {
		this.child = children;
	}
	public static Node getInstance(Identifier nonterminal, Node rule) {
		Pair<Identifier, Node> children = new Pair<Identifier, Node>(nonterminal, rule);
		if (! instances.containsKey(children)) {
			Rule r = new Rule(children);
			instances.put(children, r);
			if (! instanceMap.containsKey(nonterminal)) {
				instanceMap.put(nonterminal, new ArrayList<Rule>());
			}
			instanceMap.get(nonterminal).add(r);
		}
		return instances.get(children);
	}
	public static ArrayList<Rule> getRulesForIdentifier(Identifier id) {
		return instanceMap.get(id);
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
