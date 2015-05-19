package language;

// Convert language to a string
public class Printer implements Visitor<Void> {
	StringBuilder builder = new StringBuilder();
	public Printer(Node node) {
		if (node instanceof Identifier) {
			for (Identifier nonterminal : Identifier.list()) {
				Rule.getInstance(nonterminal).accept(this);
			}
		} else {
			node.accept(this);
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
		builder.append('\'');
		builder.append(node.symbol);
		builder.append('\'');
		return null;
	}

	@Override
	public Void visit(Sequence node) {
		node.child.left.accept(this);
		builder.append(" "); // FIXME
		node.child.right.accept(this);
		return null;
	}

	@Override
	public Void visit(Or node) {
		builder.append('(');
		node.child.left.accept(this);
		builder.append('|');
		node.child.right.accept(this);
		builder.append(')');
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
	public Void visit(Identifier nonterminal) {
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
	@Override
	public Void visit(Any any) {
		builder.append("<char>");
		return null;
	}
	@Override
	public Void visit(Group group) {
		group.child.accept(this);
		return null;
	}
}
