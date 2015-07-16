package languageV2.traversal;

import java.util.HashSet;
import java.util.Set;

import languageV2.Language;
import languageV2.Node;

public class Nullable extends AbstractVisitor<Boolean> {
	Set<Node<String,Void>> nulls = new HashSet<Node<String,Void>>();
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
	public Boolean set(Node<Node<?,?>,Node<?,?>> set) {
		if (set == Language.reject) return false;
		return g.accept(this, set.left) || g.accept(this, set.right);
	}
	public Boolean id(Node<String,Void> id) {
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
	public Boolean rule(Node<Node<String,Void>,Node<?,?>> rule) {
		return g.accept(this, rule.right);
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
