package com.dictorobitary;

/**
 * A node in grammar graph data structure.
 * 
 * @author Joey Lawrance
 *
 * @param <L>
 * @param <R>
 */
final public class Node<L,R> {
	public enum Tag {
		SYMBOL,	LIST, SET, ID, RULE, ACTION
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
	/**
	 * Accept visitor into a language.
	 * 
	 * @param visitor
	 * @param language
	 * @return
	 */
	public static <T> T accept(Visitor<T> visitor, Node<?,?> language) {
		switch(language.tag) {
		case ID:
			visitor.getWorkList().todo((Node<String,Void>)language);
			return visitor.id((Node<String,Void>)language);
		case LIST:
			return visitor.list((Node<Node<?,?>,Node<?,?>>)language);
		case SET:
			return visitor.set((Node<Node<?,?>,Node<?,?>>)language);
		case SYMBOL:
			return visitor.symbol((Node<Character,Character>)language);
		default:
			return null;
		}
	}
}
