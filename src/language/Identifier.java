package language;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// Language node representing an identifier (a terminal or nonterminal)
public class Identifier implements Node {
	private static final Map<String, Identifier> instances = new HashMap<String, Identifier>();
	public final String label;
	private Identifier(String label) {
		this.label = label;
		// Default rule
		Rule.create(this, EmptySet.getInstance());
	}
	public static Identifier getInstance(String label) {
		if (! instances.containsKey(label)) {
			instances.put(label, new Identifier(label));
		}
		return instances.get(label);
	}
	public static Collection<Identifier> list() {
		return instances.values();
	}
	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
	public void derive(Node... nodes) {
		Node rhs = Rule.getInstance(this).child.right;
		Rule.create(this, Or.getInstance(rhs, Sequence.getInstance(nodes, 0)));
	}
}
