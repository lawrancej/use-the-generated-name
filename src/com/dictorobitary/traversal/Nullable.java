package com.dictorobitary.traversal;

import java.util.HashSet;
import java.util.Set;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;
import com.dictorobitary.Node;

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
		boolean result = Node.accept(this, list.left) && Node.accept(this, list.right);
		return result;
	}
	public Boolean reject(Node<?, ?> language) {
		return false;
	}
	public Boolean set(Node<Node<?,?>,Node<?,?>> set) {
		return Node.accept(this, set.left) || Node.accept(this, set.right);
	}
	public Boolean id(Node<String,Void> id) {
		if (nulls.contains(id)) return true;
		if (todo.visited(id)) {
			return nulls.contains(id);
		} else {
			boolean result = g.acceptRule(this, id);
			if (result) {
				nulls.add(id);
			}
			return result;
		}
	}
	public Boolean rule(Node<Node<String,Void>,Node<?,?>> rule) {
		if (nulls.contains(rule.left)) return true;
		return Node.accept(this, rule.right);
	}
	public Boolean bottom() {
		return false;
	}
	public Boolean reduce(Boolean accumulator, Boolean current) {
		return current;
	}
	// Return only the first result.
	public boolean done(Boolean accumulator) {
		return true;
	}
}
