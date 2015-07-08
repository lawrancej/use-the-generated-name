package languageV2.traversal;

import languageV2.Language;
import util.Node;
import util.TaggedDataPair;

// Compute the first set
public class FirstSet extends AbstractVisitor<Node<?>> {
	public FirstSet(Language g) {
		super(g);
	}
	public Node<?> symbol(Node<Character> c) {
		return c;
	}
	public Node<?> loop(Node<Node<?>> loop) {
		return g.accept(this, loop.data);
	}
	public Node<?> list(Node<TaggedDataPair> language) {
		TaggedDataPair pair = language.data;
		if (pair == null) return bottom();
		Node<?> result = g.accept(this, pair.left);
		if (g.nullable(pair.left)) {
			result = g.or(result, g.accept(this, pair.right));
		}
		return result;
	}
	public Node<?> set(Node<TaggedDataPair> language) {
		TaggedDataPair set = language.data;
		if (set == null) return bottom();
		return g.or(g.accept(this, set.left), g.accept(this, set.right));
	}
	public Node<?> id(Language.Id id) {
		return bottom();
	}
	public Node<?> rule(Language.Id id, Node<?> rhs) {
		return g.accept(this, rhs);
	}
	public Node<?> bottom() {
		return Language.reject;
	}
	public Node<?> reduce(Node<?> accumulator, Node<?> current) {
		return g.or(accumulator, current);
	}
	@Override
	public boolean done(Node<?> accumulator) {
		return false;
	}
}
