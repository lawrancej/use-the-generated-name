package language;

import java.util.HashSet;
import java.util.Set;

public class NonterminalSet implements Visitor<Set<Nonterminal>>{
	Set<Nonterminal> result = new HashSet<Nonterminal>();

	@Override
	public Set<Nonterminal> visit(EmptySet node) {
		return result;
	}

	@Override
	public Set<Nonterminal> visit(EmptyString node) {
		return result;
	}

	@Override
	public Set<Nonterminal> visit(Symbol node) {
		return result;
	}

	@Override
	public Set<Nonterminal> visit(Sequence node) {
		node.child.left.accept(this);
		node.child.right.accept(this);
		return result;
	}

	@Override
	public Set<Nonterminal> visit(Or node) {
		node.child.left.accept(this);
		node.child.right.accept(this);
		return result;
	}

	@Override
	public Set<Nonterminal> visit(Star node) {
		node.child.accept(this);
		return result;
	}

	@Override
	public Set<Nonterminal> visit(Nonterminal nonterminal) {
		if (!result.contains(nonterminal)) {
			result.add(nonterminal);
			nonterminal.rule.accept(this);
		}
		return result;
	}

	@Override
	public Set<Nonterminal> visit(Rule rule) {
		rule.child.right.accept(this);
		return result;
	}

}
