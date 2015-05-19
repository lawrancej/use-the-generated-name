package language;

import java.util.HashSet;
import java.util.Set;

// Computes the derivative of a language with respect to a character.
// See: http://matt.might.net/articles/parsing-with-derivatives/
public class Derivative implements Visitor<Node> {
	public char c;
	public Set<Identifier> visited = new HashSet<Identifier>();
	private static Derivative derivative = new Derivative();

	private Derivative() {
		
	}
	// Compute Dc(regex)
	public static Node derivative(Node regex, char c) {
		derivative.c = c;
		derivative.visited.clear();
		return regex.accept(derivative);
	}

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
	public Node visit(Identifier id) {
		Identifier derivative = Identifier.getInstance("D" + c + id.label);
		
		if (!visited.contains(id)) {
			visited.add(id);
			derivative.derive(Rule.getInstance(id).accept(this));
			return derivative;
		} else {
			return derivative;
		}
	}

	@Override
	public Node visit(Rule rule) {
		return rule.child.right.accept(this);
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
