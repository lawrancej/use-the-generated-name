package com.dictorobitary;

import java.util.Map;

/**
 * A node in grammar graph data structure.
 * 
 * A node will be a 64-bit structure:
 * 4 bits for the tag
 * 30 bits for the left
 * 30 bits for the right
 * the identifier is the location in the pool.
 * 
 * SYMBOL: left -> char, right -> char
 * LIST: left -> node (int), right -> node (int)
 * SET: left -> node (int), right -> node (int)
 * ID: left -> label (index into label array), right -> bit field for properties such as nullability, whether it's a token. dirty bits? ? reduction action?
 * RULE: left -> node (id), right -> node (rhs)
 * LOOP: left -> node (int), right -> symbol
 * 
 * 
 * @author Joey Lawrance
 *
 * @param <L>
 * @param <R>
 */
@SuppressWarnings("unchecked")
final public class Node<L,R> {
	public static long[] pool = new long[1024*1024*2];
	public enum Tag {
		SYMBOL,	LIST, SET, ID, RULE, LOOP,
		TOKEN, ACTION, RESULT, SKIP
	}
	private final int id;
	private final Tag tag;
	private final L left;
	private final R right;
	public static int allocations = 0;
	public static <L,R> Tag tag(Node<L,R> language) {
		return language.tag;
	}
	public static <L,R> L left(Node<L,R> language) {
		return language.left;
	}
	public static <L,R> R right(Node<L,R> language) {
		return language.right;
	}
	public static <L,R> int id(Node<L,R> language) {
		return language.id;
	}
	private Node(Tag type, L left, R right) {
//		this.id = rand.nextLong();
		this.id = allocations;
		this.tag = type;
		this.left = left;
		this.right = right;
		allocations++;
	}
	public static <Left, Right, Key> Node<Left,Right> getCached(Map<Key, Node<Left,Right>> cache, Key key) {
		return cache.get(key);
	}
	public static <Left, Right,Key> Node<Left,Right> createCached(Map<Key, Node<Left,Right>> cache, Key key, Tag type, Left left, Right right) {
		if (!cache.containsKey(key)) {
			Node<Left,Right> result = Node.create(type, left, right);
			cache.put(key, result);
			return result;
		}
		return cache.get(key);
	}
	public boolean equals(Object other) {
		return this.hashCode() == other.hashCode();
	}
	public int hashCode() {
		return (int)id(this);
	}
	// Handy shortcut for the constructor call
	public static <Left,Right> Node<Left,Right> create(Tag type, Left left, Right right) {
		return new Node<Left,Right>(type, left, right);
	}
	/**
	 * Accept a visitor.
	 * 
	 * @param visitor
	 * @param language
	 * @return
	 */
	public static <T> T accept(Visitor<T> visitor, Node<?,?> language) {
		switch(Node.tag(language)) {
		case ID:
			visitor.getWorkList().todo((Node<String,Void>)language);
			return visitor.id((Node<String,Void>)language);
		case LIST:
			if (language == Language.empty) {
				return visitor.empty(language);
			}
			return visitor.list((Node<Node<?,?>,Node<?,?>>)language);
		case SET:
			if (language == Language.reject) {
				return visitor.reject(language);
			}
			return visitor.set((Node<Node<?,?>,Node<?,?>>)language);
		case SYMBOL:
			if (language == Language.any) {
				return visitor.any(language);
			}
			return visitor.symbol((Node<Character,Character>)language);
		case LOOP:
			return visitor.loop((Node<Node<?,?>,Node<?,?>>)language);
		default:
			return null;
		}
	}
}
