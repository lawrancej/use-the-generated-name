package com.dictorobitary.traversal;

import java.util.HashSet;
import java.util.Set;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;

public class Token extends AbstractVisitor<Boolean> {
	Set<Integer> tokens = new HashSet<Integer>();
	public Token(Language g) {
		super(g);
	}
	public Boolean any(int language) {
		return true;
	}
	public Boolean symbol(int language, char symbol) {
		return true;
	}
	public Boolean range(int language, char from, char to) {
		return true;
	}
	public Boolean empty(int language) {
		return true;
	}
	public Boolean list(int language) {
		return g.accept(this, g.left(language)) && g.accept(this, g.right(language));
	}
	public Boolean loop(int language) {
		return g.accept(this, g.left(language));
	}
	public Boolean reject(int language) {
		return true;
	}
	public Boolean set(int language) {
		return g.accept(this, g.left(language)) && g.accept(this, g.right(language));
	}
	public Boolean id(int id) {
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
	public Boolean rule(int rule) {
		if (tokens.contains(g.left(rule))) return true;
		return g.accept(this, g.right(rule));
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
