package languageV2.traversal;

import java.util.HashSet;
import java.util.Set;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.Node;
import util.TaggedDataPair;

public class Nullable extends AbstractVisitor<Boolean> {
	Set<Language.Id> nulls = new HashSet<Language.Id>();
	public Nullable(Language g) {
		super(g);
	}
	public Boolean symbol(Node<Character> c) {
		return false;
	}
	public Boolean list(Node<TaggedDataPair> language) {
		TaggedDataPair list = language.data;
		if (list == null) return true;
		boolean result = g.accept(this, list.left) && g.accept(this, list.right);
		return result;
	}
	public Boolean loop(Node<Node<?>> language) {
		return true;
	}
	public Boolean set(Node<SetOfLanguages> language) {
		SetOfLanguages set = language.data;
		if (set == null) return false;
		for (Node<?> l : set) {
			if (g.accept(this, l)) {
				return true;
			}
		}
		return false;
	}
	public Boolean id(Language.Id id) {
		if (todo.visited(id)) {
			return nulls.contains(id);
		} else {
			boolean result = g.acceptRule(this, id);
			if (result) {
				nulls.add(id);
			}
			return result;
		}
	}
	public Boolean rule(Language.Id id, Node<?> rhs) {
		return g.accept(this, rhs);
	}
	public Boolean bottom() {
		return false;
	}
	public Boolean reduce(Boolean accumulator, Boolean current) {
		return current;
	}
	// Return only the first result.
	public boolean done(Boolean accumulator) {
		return true;
	}
}
