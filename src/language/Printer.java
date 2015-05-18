package language;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

// Convert regex to a string
public class Printer implements Visitor<Void> {
	StringBuilder builder = new StringBuilder();
	public Printer(Node node) {
		Set<Nonterminal> nonterminals = node.accept(new NonterminalSet());
		if (nonterminals.isEmpty()) {
			node.accept(this);
		} else {
			for (Nonterminal nonterminal : nonterminals) {
				nonterminal.rule.accept(this);
			}
		}
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
		builder.append("<");
		builder.append(nonterminal.label);
		builder.append(">");
		return null;
	}
	@Override
	public Void visit(Rule rule) {
		rule.child.left.accept(this);
		builder.append(" ::= ");
		rule.child.right.accept(this);
		builder.append("\n");
		return null;
	}
}
