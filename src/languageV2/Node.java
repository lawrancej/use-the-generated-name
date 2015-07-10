package languageV2;

/**
 * A language is a set of strings (lists of symbols).
 * 
 * Specifying a finite language requires symbols, lists, and sets.
 * Specifying an infinite language also requires repetition or recursion.
 * Loops specify repetition, and identifiers enable recursion.
 */
public class Node<L,R> {
	public enum Tag {
		// Finite language specifiers
		SYMBOL,	LIST, SET,
		// Infinite language specifiers
		LOOP, ID,
	}
	public final Tag tag;
	public final L left;
	public final R right;
	public static int allocations = 0;
	protected Node(Tag type, L left, R right) {
		this.tag = type;
		this.left = left;
		this.right = right;
		allocations++;
	}
	// Handy shortcut for the constructor call
	public static <Left,Right> Node<Left,Right> create(Tag type, Left left, Right right) {
		return new Node<Left,Right>(type, left, right);
	}
}
