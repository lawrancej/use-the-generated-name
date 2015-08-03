package com.dictorobitary.traversal;

import java.util.HashSet;
import java.util.Set;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;
import com.dictorobitary.Node;

public class Token extends AbstractVisitor<Boolean> {
	Set<Node<String,Void>> tokens = new HashSet<Node<String,Void>>();
	public Token(Language g) {
		super(g);
	}
	public Boolean any(Node<?, ?> language) {
		return true;
	}
	public Boolean symbol(Node<Character, Character> language) {
		return true;
	}
	public Boolean empty(Node<?, ?> language) {
		return true;
	}
	public Boolean list(Node<Node<?, ?>, Node<?, ?>> language) {
		return Node.accept(this, language.left) && Node.accept(this, language.right);
	}
	public Boolean loop(Node<Node<?, ?>, Node<?, ?>> language) {
		return Node.accept(this, language.left);
	}
	public Boolean reject(Node<?, ?> language) {
		return true;
	}
	public Boolean set(Node<Node<?, ?>, Node<?, ?>> language) {
		return Node.accept(this, language.left) && Node.accept(this, language.right);
	}
	public Boolean id(Node<String, Void> id) {
		// If we saw this rule already, it's not a token
		if (todo.visited(id)) {
			return false;
		} else {
			if (tokens.contains(id)) return true;
			// Recursive rules aren't tokens
			if (getWorkList().visiting(id)) return false;
			boolean result = g.acceptRule(this, id);
			if (result) {
				tokens.add(id);
			}
			return result;
		}
	}
	public Boolean rule(Node<Node<String, Void>, Node<?, ?>> rule) {
		if (tokens.contains(rule.left)) return true;
		return Node.accept(this, rule.right);
	}
	public Boolean bottom() {
		return false;
	}
	public boolean done(Boolean accumulator) {
		return true;
	}
	public Boolean reduce(Boolean accumulator, Boolean current) {
		return accumulator || current;
	}
}
