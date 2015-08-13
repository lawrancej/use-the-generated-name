package com.dictorobitary.traversal;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;

public class Derivative extends AbstractVisitor<Integer> {
	public Character c;
	private Map<Integer, Integer> ids = new LinkedHashMap<Integer, Integer>();
	public Derivative(Language g) {
		super(g);
	}
	public Integer any(int language) {
		// D(.) = e
		return g.empty;
	}
	public Integer symbol(int language) {
		// Dc(c) = e
		if (this.c == g.left(language) || (this.c > g.left(language) && this.c <= g.right(language))) {
			return g.empty;
		}
		// Dc(c') = 0
		return bottom();
	}
	public Integer empty(int language) {
		// Dc(e) = 0
		return bottom();
	}
	public Integer list(int list) {
		// Dc(ab) = Dc(a)b + nullable(a)Dc(b)
		int result = g.list(g.accept(this, g.left(list)), g.right(list));
		if (g.get.nullable.compute(g.left(list))) {
			return g.or(result, g.accept(this, g.right(list)));
		}
		return result;
	}
	public Integer loop(int language) {
		// Dc(a*) = Dc(a)a* = Dc(aa*)
//		return g.accept(this, g.list(g.left, language));
		return g.list(g.accept(this, g.left(language)), language);
	}
	public Integer reject(int langauge) {
		// Dc(0) = 0
		return bottom();
	}
	public Integer set(int set) {
		// Dc(a+b) = Dc(a) + Dc(b)
		return g.or(g.accept(this, g.left(set)), g.accept(this, g.right(set)));
	}
	private int getReplacement(int id) {
		if (!ids.containsKey(id)) {
			int replacement = g.id();
			ids.put(id, replacement);
			return replacement;
		}
		else {
			return ids.get(id);
		}
	}
	public Integer id(int id) {
		if (todo.visited(id)) {
			// By this point, we've seen the identifier on the rhs before.
			// If the identifier derives a non-empty set, return the identifier
			if (ids.containsKey(id)) {
				return ids.get(id);
			}
			// Handle left-recursion: return DcId if we're visiting Id -> Id
			if (todo.visiting(id)) {
				// Technically, this is all we have to do
				// Everything else is an optimization
				return getReplacement(id);
			}
			// Otherwise, return the empty set
			return bottom();
		}
		// Visit rule Id -> rhs, if we haven't already visited it.
		else {
			return g.acceptRule(this, id);
		}
	}
	public Integer rule(int rule) {
		// By this point, we've seen the identifier on the rhs before.
		// If the identifier derives a non-empty set, return the identifier
		if (ids.containsKey(g.left(rule))) {
			return ids.get(g.left(rule));
		}
		// Visit the rhs
		int derivation = g.accept(this,  g.right(rule));
		
		// Don't create a rule that rejects or is empty
		if (derivation == g.reject || derivation == g.empty) {
			return derivation;
		}
		// Create a new rule
		return g.rule(getReplacement(g.left(rule)), derivation);
	}
	public Integer bottom() {
		return g.reject;
	}
	public Integer reduce(Integer accumulator, Integer current) {
		if (accumulator == bottom()) return current;
		else return accumulator;
	}
	public boolean done(Integer accumulator) {
		return true;
	}
	public void begin() {
		ids.clear();
	}
}
