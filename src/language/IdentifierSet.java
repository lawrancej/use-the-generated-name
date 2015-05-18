package language;

import java.util.HashSet;
import java.util.Set;

public class IdentifierSet implements Visitor<Set<Identifier>>{
	Set<Identifier> result = new HashSet<Identifier>();

	@Override
	public Set<Identifier> visit(EmptySet node) {
		return result;
	}

	@Override
	public Set<Identifier> visit(EmptyString node) {
		return result;
	}

	@Override
	public Set<Identifier> visit(Symbol node) {
		return result;
	}

	@Override
	public Set<Identifier> visit(Sequence node) {
		node.child.left.accept(this);
		node.child.right.accept(this);
		return result;
	}

	@Override
	public Set<Identifier> visit(Or node) {
		node.child.left.accept(this);
		node.child.right.accept(this);
		return result;
	}

	@Override
	public Set<Identifier> visit(Star node) {
		node.child.accept(this);
		return result;
	}

	@Override
	public Set<Identifier> visit(Identifier nonterminal) {
		if (!result.contains(nonterminal)) {
			result.add(nonterminal);
			nonterminal.rule.accept(this);
		}
		return result;
	}

	@Override
	public Set<Identifier> visit(Rule rule) {
		rule.child.right.accept(this);
		return result;
	}

}
