package com.dictorobitary.traversal;

import java.util.HashSet;
import java.util.Set;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;
import com.dictorobitary.Node;

/**
 * Determine if a language can derive the empty string
 * 
 * @author Joseph Lawrance
 *
 */
public class Nullable extends AbstractVisitor<Boolean> {
	Set<Node<String,Void>> nulls = new HashSet<Node<String,Void>>();
	public Nullable(Language g) {
		super(g);
	}
	public Boolean any(Node<?, ?> language) {
		return false;
	}
	public Boolean symbol(Node<Character,Character> c) {
		return false;
	}
	public Boolean empty(Node<?, ?> language) {
		return true;
	}
	public Boolean list(Node<Node<?,?>,Node<?,?>> list) {
		boolean result = Node.accept(this, Node.left(list)) && Node.accept(this, Node.right(list));
		return result;
	}
	public Boolean loop(Node<Node<?,?>,Node<?,?>> language) {
		return Node.right(language) == Language.any;
	}
	public Boolean reject(Node<?, ?> language) {
		return false;
	}
	public Boolean set(Node<Node<?,?>,Node<?,?>> set) {
		return Node.accept(this, Node.left(set)) || Node.accept(this, Node.right(set));
	}
	public Boolean id(Node<String,Void> id) {
		if (todo.visited(id)) {
			return nulls.contains(id);
		} else {
			if (nulls.contains(id)) return true;
			boolean result = g.acceptRule(this, id);
			if (result) {
				nulls.add(id);
			}
			return result;
		}
	}
	public Boolean rule(Node<Node<String,Void>,Node<?,?>> rule) {
		if (nulls.contains(Node.left(rule))) return true;
		return Node.accept(this, Node.right(rule));
	}
	public Boolean bottom() {
		return false;
	}
	public Boolean reduce(Boolean accumulator, Boolean current) {
		return current;
	}
	public void begin() {
//		nulls.clear();
	}
	// Return only the first result.
	public boolean done(Boolean accumulator) {
		return true;
	}
}
