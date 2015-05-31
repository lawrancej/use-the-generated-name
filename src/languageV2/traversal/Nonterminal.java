package languageV2.traversal;

import languageV2.Grammar;
import languageV2.SetOfLanguages;
import util.TaggedData;
import util.TaggedDataPair;

/**
 * Is the identifier a nonterminal?
 */
public class Nonterminal extends AbstractVisitor<Boolean> {
	String label;
	public Nonterminal(Grammar g, String label) {
		super(g, new WorkList<String>());
		this.label = label;
	}
	public Boolean symbol(Character c) {
		return false;
	}
	public Boolean list(TaggedDataPair list) {
		if (list == null) return false;
		return g.visit(this, list.left) || g.visit(this, list.right);
	}
	public Boolean loop(TaggedData<?> language) {
		return g.visit(this, language);
	}
	public Boolean set(SetOfLanguages set) {
		boolean result = false;
		if (set == null) return result;
		for (TaggedData<?> l : set) {
			result = result || g.visit(this, l);
		}
		return result;
	}
	public Boolean id(String id) {
		return todo.visited(label);
	}
	public Boolean rule(String id, TaggedData<?> rhs) {
		return g.visit(this, rhs);
	}
	public Boolean bottom() {
		return false;
	}
	public Boolean reduce(Boolean accumulator, Boolean current) {
		return accumulator || current;
	}
	@Override
	public boolean done(Boolean accumulator) {
		return accumulator;
	}
}
