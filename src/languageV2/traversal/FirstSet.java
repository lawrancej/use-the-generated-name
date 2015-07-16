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
	public Node<?,?> id(Node<String,Void> id) {
		return bottom();
	}
	public Node<?,?> rule(Node<Node<String,Void>,Node<?,?>> rule) {
		return g.accept(this, rule.right);
	}
	public Node<?,?> bottom() {
		return Language.reject;
	}
	public Node<?,?> reduce(Node<?,?> accumulator, Node<?,?> current) {
		return g.or(accumulator, current);
	}
	public boolean done(Node<?,?> accumulator) {
		return false;
	}
}
