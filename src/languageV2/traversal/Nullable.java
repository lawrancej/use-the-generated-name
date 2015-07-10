package languageV2.traversal;

import java.util.HashSet;
import java.util.Set;

import languageV2.Language;
import languageV2.Node;

public class Nullable extends AbstractVisitor<Boolean> {
	Set<Language.Id> nulls = new HashSet<Language.Id>();
	public Nullable(Language g) {
		super(g);
	}
	public Boolean symbol(Node<Character,Character> c) {
		return false;
	}
	public Boolean list(Node<Node<?,?>,Node<?,?>> list) {
		if (list == Language.empty) return true;
		boolean result = g.accept(this, list.left) && g.accept(this, list.right);
		return result;
	}
	public Boolean loop(Node<Node<?,?>,Node<?,?>> language) {
		return true;
	}
	public Boolean set(Node<Node<?,?>,Node<?,?>> set) {
		if (set == Language.reject) return false;
		return g.accept(this, set.left) || g.accept(this, set.right);
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
	public Boolean rule(Language.Id id, Node<?,?> rhs) {
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
	@Override
	public Boolean end(Boolean accumulator) {
		return accumulator;
	}
}
