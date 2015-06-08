package languageV2.traversal;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.Node;
import util.TaggedData;
import util.TaggedDataPair;

/**
 * Is the identifier a nonterminal?
 */
public class Nonterminal extends AbstractVisitor<Boolean> {
	String label;
	public Nonterminal(Language g, String label) {
		super(g, new WorkList<String>());
		this.label = label;
	}
	public Boolean symbol(Character c) {
		return false;
	}
	public Boolean list(Node<TaggedData<?>> list) {
		if (list == null) return false;
		return g.visit(this, list.data) || list(list.next);
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
