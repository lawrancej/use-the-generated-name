package language;

// Computes the derivative of a language with respect to a character.
// See: http://matt.might.net/articles/parsing-with-derivatives/
public class Derivative implements Visitor<Node> {
	public char c;

	@Override
	public Node visit(EmptySet node) {
		return node;
	}

	@Override
	public Node visit(EmptyString node) {
		return EmptySet.getInstance();
	}

	@Override
	public Node visit(Symbol node) {
		if (node.symbol == c) {
			return EmptyString.getInstance();
		} else {
			return EmptySet.getInstance();
		}
	}

	@Override
	public Node visit(Sequence node) {
		Node result = Sequence.getInstance(node.child.left.accept(this), node.child.right);
		if (Language.nullable(node.child.left)) {
			return Or.getInstance(result, node.child.right.accept(this));
		}
		return result;
	}

	@Override
	public Node visit(Or node) {
		return Or.getInstance(node.child.left.accept(this), node.child.right.accept(this));
	}

	@Override
	public Node visit(Star node) {
		return Sequence.getInstance(node.child.accept(this), node);
	}

	@Override
	public Node visit(Identifier nonterminal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node visit(Rule rule) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node visit(Any any) {
		return EmptyString.getInstance();
	}

	@Override
	public Node visit(Group group) {
		return Group.getInstance(group.child.accept(this));
	}
}
