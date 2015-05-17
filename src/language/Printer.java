package language;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

// Convert regex to a string
public class Printer implements Visitor<Void> {
	StringBuilder builder = new StringBuilder();
	Queue<Nonterminal> toVisit = new LinkedList<Nonterminal>();
	HashSet<Nonterminal> visited = new HashSet<Nonterminal>();
	public Printer(Node node) {
		node.accept(this);
	}
	@Override
	public Void visit(EmptySet node) {
		builder.append("\u2205");
		return null;
	}

	@Override
	public Void visit(EmptyString node) {
		builder.append("\u03b5");
		return null;
	}

	@Override
	public Void visit(Symbol node) {
		builder.append(node.symbol);
		return null;
	}

	@Override
	public Void visit(Sequence node) {
		node.child.left.accept(this);
		node.child.right.accept(this);
		return null;
	}

	@Override
	public Void visit(Or node) {
		node.child.left.accept(this);
		builder.append('|');
		node.child.right.accept(this);
		return null;
	}

	@Override
	public Void visit(Star node) {
		builder.append('(');
		node.child.accept(this);
		builder.append(")*");
		return null;
	}
	
	@Override
	public String toString() {
		return builder.toString();
	}
	@Override
	public Void visit(Nonterminal nonterminal) {
		builder.append(nonterminal.label);
		if (!visited.contains(nonterminal)) {
			visited.add(nonterminal);
			builder.append(" -> ");
			nonterminal.rule.accept(this);
			builder.append("\n");
		}
		return null;
	}
	@Override
	public Void visit(Rule rule) {
		return null;
	}
}
