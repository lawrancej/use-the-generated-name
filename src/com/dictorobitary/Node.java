package com.dictorobitary;

import java.util.Map;

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
	public enum Tag {
		SYMBOL,	LIST, SET, ID, RULE, ACTION
	}
	public final Tag tag;
	public final L left;
	public final R right;
	public static int allocations = 0;
	private Node(Tag type, L left, R right) {
		this.tag = type;
		this.left = left;
		this.right = right;
		allocations++;
	}
	public static <Left, Right> Node<Left,Right> createCached(Map<Integer, Node<Left,Right>> cache, int key, Tag type, Left left, Right right) {
		if (!cache.containsKey(key)) {
			Node<Left,Right> result = Node.create(type, left, right);
			cache.put(key, result);
			return result;
		}
		return cache.get(key);
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
		case RULE:
			return visitor.rule((Node<Node<String, Void>, Node<?, ?>>) language);
		default:
			return null;
		}
	}
}
