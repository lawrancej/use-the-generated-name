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
	public Boolean symbol(Node<Character,Character> c) {
		return false;
	}
	public Boolean list(Node<Node<?,?>,Node<?,?>> list) {
		if (list == Language.empty) return true;
		boolean result = Node.accept(this, list.left) && Node.accept(this, list.right);
		return result;
	}
	public Boolean set(Node<Node<?,?>,Node<?,?>> set) {
		if (set == Language.reject) return false;
		return Node.accept(this, set.left) || Node.accept(this, set.right);
	}
	public Boolean id(Node<String,Void> id) {
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
