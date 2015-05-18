package language;

// A visitor traverses nodes without mixing code in those nodes.
public interface Visitor<T> {
	T visit(EmptySet node);
	T visit(EmptyString node);
	T visit(Symbol node);
	T visit(Sequence node);
	T visit(Or node);
	T visit(Star node);
	T visit(Identifier nonterminal);
	T visit(Rule rule);
	T visit(Any any);
	T visit(Group group);
}
