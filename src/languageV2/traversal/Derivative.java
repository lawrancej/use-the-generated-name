package languageV2.traversal;

import java.util.HashMap;
import java.util.Map;

import languageV2.Language;
import languageV2.Node;

public class Derivative extends AbstractVisitor<Node<?,?>> {
	public Character c;
	Map<Language.Id, Language.Id> ids = new HashMap<Language.Id, Language.Id>();
	public Derivative(Language g) {
		super(g);
	}
	public Node<?,?> symbol(Node<Character,Character> language) {
		// Dc(c|.) = e
		if (language == Language.any || this.c == language.left || (this.c > language.left && this.c <= language.right)) {
			return Language.empty;
		}
		// Dc(c') = 0
		return bottom();
	}
	public Node<?,?> list(Node<Node<?,?>,Node<?,?>> list) {
		// Dc(e) = 0
		if (list == Language.empty) return bottom();
		// Dc(ab) = Dc(a)b + nullable(a)Dc(b)
		Node<?,?> result = g.list(g.accept(this, list.left), list.right);
		if (g.nullable(list.left)) {
			return g.or(result, g.accept(this, list.right));
		}
		return result;
	}
	public Node<?,?> loop(Node<Node<?,?>,Node<?,?>> loop) {
		// D(a*)=D(a)a*
//		return g.visit(this, g.list(language, loop));
		return g.list(g.accept(this, loop.left), loop);
	}
	public Node<?,?> set(Node<Node<?,?>,Node<?,?>> set) {
		// Dc(0) = 0
		if (set == Language.reject) return bottom();
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
	public Node<?,?> id(Language.Id id) {
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
	public Node<?,?> rule(Language.Id id, Node<?,?> rhs) {
		// Visit the rhs
		Node<?,?> derivation = g.accept(this,  rhs);
		
		// Don't create a rule that rejects or is empty (or doesn't contain its replacement?)
		if (derivation == Language.reject || derivation == Language.empty) {
			return derivation;
		}
		if (ids.containsKey(id) && todo.visited(ids.get(id))) {
			return derivation;
		}
		// Create a new rule
		return g.derives(getReplacement(id), derivation);
	}
	public Node<?,?> bottom() {
		return Language.reject;
	}
	public Node<?,?> reduce(Node<?,?> accumulator, Node<?,?> current) {
		if (accumulator == bottom()) return current;
		else return accumulator;
	}
	public boolean done(Node<?,?> accumulator) {
		return false;
	}
	public void begin() {
		ids.clear();
	}
	@Override
	public Node<?, ?> end(Node<?, ?> accumulator) {
		if (ids.size() > 0) {
			return g.derives(g.id(), accumulator);
		} else {
			return accumulator;
		}
	}
}
