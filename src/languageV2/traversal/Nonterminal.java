package languageV2.traversal;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.TaggedData;
import util.TaggedDataPair;

/**
 * Is the identifier a nonterminal?
 */
public class Nonterminal extends AbstractVisitor<Boolean> {
	Language.Id label;
	public Nonterminal(Language g, String label) {
		super(g, new WorkList<Language.Id>());
		this.label = g.id(label);
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
	public Boolean id(Language.Id id) {
		return todo.visited(label);
	}
	public Boolean rule(Language.Id id, TaggedData<?> rhs) {
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
