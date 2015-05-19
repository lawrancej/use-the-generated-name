package language;

import java.util.HashSet;
import java.util.Set;

// Does the language match the empty string?
public class Nullable implements Visitor<Boolean> {
	Set<Identifier> visited = new HashSet<Identifier>();
	private Nullable() {
		
	}
	public static boolean nullable(Node node) {
		return node.accept(new Nullable());
	}

	@Override
	public Boolean visit(EmptySet node) {
		return false;
	}

	@Override
	public Boolean visit(EmptyString node) {
		return true;
	}

	@Override
	public Boolean visit(Symbol node) {
		return false;
	}

	@Override
	public Boolean visit(Sequence node) {
		return node.child.left.accept(this) && node.child.right.accept(this);
	}

	@Override
	public Boolean visit(Or node) {
		return node.child.left.accept(this) || node.child.right.accept(this);
	}

	@Override
	public Boolean visit(Star node) {
		return true;
	}

	@Override
	public Boolean visit(Identifier id) {
		if (!visited.contains(id)) {
			visited.add(id);
			return Rule.getInstance(id).child.right.accept(this);
		}
		return false;
	}

	@Override
	public Boolean visit(Rule rule) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(Any any) {
		return false;
	}

	@Override
	public Boolean visit(Group group) {
		return group.child.accept(this);
	}
}
