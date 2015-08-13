package com.dictorobitary.traversal;

import java.util.HashSet;
import java.util.Set;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;

/**
 * Determine if a language can derive the empty string
 * 
 * @author Joseph Lawrance
 *
 */
public class Nullable extends AbstractVisitor<Boolean> {
	Set<Integer> nulls = new HashSet<Integer>();
	public Nullable(Language g) {
		super(g);
	}
	public Boolean any(int language) {
		return false;
	}
	public Boolean symbol(int c) {
		return false;
	}
	public Boolean empty(int language) {
		return true;
	}
	public Boolean list(int list) {
		boolean result = g.accept(this, g.left(list)) && g.accept(this, g.right(list));
		return result;
	}
	public Boolean loop(int language) {
		return g.right(language) == g.any;
	}
	public Boolean reject(int language) {
		return false;
	}
	public Boolean set(int set) {
		return g.accept(this, g.left(set)) || g.accept(this, g.right(set));
	}
	public Boolean id(int id) {
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
	public Boolean rule(int rule) {
		if (nulls.contains(g.left(rule))) return true;
		return g.accept(this, g.right(rule));
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
