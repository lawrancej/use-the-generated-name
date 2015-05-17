package language;

// A visitor traverses a tree without placing code in the tree nodes themselves.
public interface Visitor<T> {
	T visit(EmptySet node);
	T visit(EmptyString node);
	T visit(Symbol node);
	T visit(Sequence node);
	T visit(Or node);
	T visit(Star node);
	T visit(Nonterminal nonterminal);
	T visit(Rule rule);
}
