package com.dictorobitary;

import java.util.Map;
import java.util.Random;

/**
 * A node in grammar graph data structure.
 * 
 * @author Joey Lawrance
 *
 * @param <L>
 * @param <R>
 */
@SuppressWarnings("unchecked")
final public class Node<L,R> {
	private static final Random rand = new Random();
	public enum Tag {
		SYMBOL,	LIST, SET, ID, RULE, ACTION, LOOP
	}
	public final long id;
	public final Tag tag;
	public final L left;
	public final R right;
	public static long allocations = 0;
	private Node(Tag type, L left, R right) {
		this.id = rand.nextLong();
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
		return (int)id;
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
		case RULE:
			visitor.getWorkList().done((Node<String,Void>)language);
			return visitor.rule((Node<Node<String,Void>,Node<?,?>>)language);
		default:
			return null;
		}
	}
}
