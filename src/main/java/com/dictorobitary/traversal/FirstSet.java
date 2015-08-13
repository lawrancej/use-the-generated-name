package com.dictorobitary.traversal;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;

// Compute the first set
public class FirstSet extends AbstractVisitor<Integer> {
	public FirstSet(Language g) {
		super(g);
	}
	public Integer any(int language) {
		return language;
	}
	public Integer symbol(int language) {
		return language;
	}
	public Integer empty(int language) {
		return bottom();
	}
	public Integer list(int pair) {
		int result = g.accept(this, g.left(pair));
		if (g.get.nullable.compute(g.left(pair))) {
			result = g.or(result, g.accept(this, g.right(pair)));
		}
		return result;
	}
	public Integer loop(int loop) {
		return g.accept(this, g.left(loop));
	}
	public Integer reject(int langauge) {
		return bottom();
	}
	public Integer set(int set) {
		return g.or(g.accept(this, g.left(set)), g.accept(this, g.right(set)));
	}
	public Integer id(int id) {
		return bottom();
	}
	public Integer rule(int rule) {
		return g.accept(this, g.right(rule));
	}
	public Integer bottom() {
		return g.reject;
	}
	public Integer reduce(Integer accumulator, Integer current) {
		return g.or(accumulator, current);
	}
	public boolean done(Integer accumulator) {
		return false;
	}
}
