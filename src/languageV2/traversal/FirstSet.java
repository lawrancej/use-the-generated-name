package languageV2.traversal;

import languageV2.Language;
import languageV2.Node;

// Compute the first set
public class FirstSet extends AbstractVisitor<Node<?,?>> {
	public FirstSet(Language g) {
		super(g);
	}
	public Node<?,?> symbol(Node<Character,Character> c) {
		return c;
	}
	public Node<?,?> loop(Node<Node<?,?>,Node<?,?>> loop) {
		return g.accept(this, loop.left);
	}
	public Node<?,?> list(Node<Node<?,?>,Node<?,?>> pair) {
		if (pair == Language.empty) return bottom();
		Node<?,?> result = g.accept(this, pair.left);
		if (g.nullable(pair.left)) {
			result = g.or(result, g.accept(this, pair.right));
		}
		return result;
	}
	public Node<?,?> set(Node<Node<?,?>,Node<?,?>> set) {
		if (set == Language.reject) return bottom();
		return g.or(g.accept(this, set.left), g.accept(this, set.right));
	}
	public Node<?,?> id(Language.Id id) {
		return bottom();
	}
	public Node<?,?> rule(Language.Id id, Node<?,?> rhs) {
		return g.accept(this, rhs);
	}
	public Node<?,?> bottom() {
		return Language.reject;
	}
	public Node<?,?> reduce(Node<?,?> accumulator, Node<?,?> current) {
		return g.or(accumulator, current);
	}
	@Override
	public boolean done(Node<?,?> accumulator) {
		return false;
	}
	@Override
	public Node<?, ?> end(Node<?, ?> accumulator) {
		return accumulator;
	}
}
