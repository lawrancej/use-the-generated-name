package language;

import java.util.HashSet;
import java.util.Set;

public class FirstSet implements Visitor<Node> {
	private Set<Identifier> visited = new HashSet<Identifier>();
	
	private FirstSet() {
		
	}
	
	public static Node firstSet(Node node) {
		return node.accept(new FirstSet());
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
		return node;
	}

	@Override
	public Node visit(Sequence node) {
		Node result = node.child.left.accept(this);
		if (Nullable.nullable(node.child.left)) {
			result = Or.getInstance(result, node.child.right.accept(this));
		}
		return result;
	}

	@Override
	public Node visit(Or node) {
		return Or.getInstance(node.child.left.accept(this), node.child.right.accept(this));
	}

	@Override
	public Node visit(Star node) {
		return node.child.accept(this);
	}

	@Override
	public Node visit(Identifier id) {
		if (!visited.contains(id)) {
			visited.add(id);
			return Rule.getInstance(id).child.right.accept(this);
		}
		return EmptySet.getInstance();
	}

	@Override
	public Node visit(Rule rule) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node visit(Any any) {
		return any;
	}

	@Override
	public Node visit(Group group) {
		return group.child.accept(this);
	}

}
