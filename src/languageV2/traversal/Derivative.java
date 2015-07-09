package languageV2.traversal;

import java.util.HashMap;
import java.util.Map;

import languageV2.Language;
import util.Node;
import util.TaggedDataPair;

public class Derivative extends AbstractVisitor<Node<?>> {
	public Character c;
	Map<Language.Id, Language.Id> ids = new HashMap<Language.Id, Language.Id>();
	public Derivative(Language g) {
		super(g);
	}
	public Node<?> symbol(Node<Character> language) {
		Character c = language.data;
		// Dc(c|.) = e
		if (c == null || this.c == c) {
			return Language.empty;
		}
		// Dc(c') = 0
		return bottom();
	}
	public Node<?> list(Node<TaggedDataPair> language) {
		TaggedDataPair list = language.data;
		// Dc(e) = 0
		if (list == null) return bottom();
		// Dc(ab) = Dc(a)b + nullable(a)Dc(b)
		Node<?> result = g.list(g.accept(this, list.left), list.right);
		if (g.nullable(list.left)) {
			return g.or(result, g.accept(this, list.right));
		}
		return result;
	}
	public Node<?> loop(Node<Node<?>> loop) {
		Node<?> language = loop.data;
//		return g.visit(this, g.list(language, loop));
		return g.list(g.accept(this, language), loop);
	}
	public Node<?> set(Node<TaggedDataPair> language) {
		TaggedDataPair set = language.data;
		// Dc(0) = 0
		if (set == null) return bottom();
		// Dc(a+b) = Dc(a) + Dc(b)
		return g.or(g.accept(this, set.left), g.accept(this, set.right));
	}
	private Language.Id getReplacement(Language.Id id) {
		if (!ids.containsKey(id)) {
			Language.Id replacement = g.id();
			ids.put(id, replacement);
			return replacement;
		}
		else {
			return ids.get(id);
		}
	}
	public Node<?> id(Language.Id id) {
		// Handle left-recursion: return DcId if we're visiting Id -> Id
		if (todo.visiting(id)) {
			return getReplacement(id);
		}
		// Visit rule Id -> rhs, if we haven't already visited it.
		if (!todo.visited(id)) {
			return g.acceptRule(this, id);
		}
		// By this point, we've seen the identifier on the rhs before.
		// If the identifier derives a non-empty set, return the identifier
		if (ids.containsKey(id)) {
			return ids.get(id);
		}
		// Otherwise, return the empty set
		return bottom();
	}
	public Node<?> rule(Language.Id id, Node<?> rhs) {
		// Visit the rhs
		Node<?> derivation = g.accept(this,  rhs);
		
		// Don't create a rule that rejects
		if (derivation == Language.reject || derivation == Language.empty) {
			return derivation;
		}
		
		// Create a new rule
		return g.derives(getReplacement(id), derivation);
	}
	public Node<?> bottom() {
		return Language.reject;
	}
	public Node<?> reduce(Node<?> accumulator, Node<?> current) {
		if (accumulator == bottom()) return current;
		else return accumulator;
	}
	public boolean done(Node<?> accumulator) {
		return false;
	}
	public void begin() {
		ids.clear();
	}
}
