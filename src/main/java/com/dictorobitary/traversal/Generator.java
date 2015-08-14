package com.dictorobitary.traversal;

import java.util.Random;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;

/**
 * Randomly generate strings in a language.
 * 
 * @author Joey Lawrance
 *
 */
public class Generator extends AbstractVisitor<StringBuilder> {

	public int depthLimit = 10;
	public int loopLimit = 5;
	Random rand = new Random();
	StringBuilder buffer;
	public Generator(Language g) {
		super(g);
	}
	public StringBuilder any(int language) {
		return buffer.append((char)(rand.nextInt(127)+1));
	}
	public StringBuilder symbol(int language, char symbol) {
		return buffer.append(symbol);
	}
	public StringBuilder range(int language, char from, char to) {
		return buffer.append((char)(rand.nextInt(to - from) + from));
	}
	public StringBuilder empty(int language) {
		return buffer;
	}
	public StringBuilder list(int language) {
		g.accept(this, g.left(language));
		g.accept(this, g.right(language));
		return buffer;
	}
	public StringBuilder loop(int language) {
		for (int iterations = rand.nextInt(loopLimit); iterations > 0; iterations--) {
			g.accept(this, g.left(language));
		}
		return buffer;
	}
	public StringBuilder reject(int language) {
		return buffer;
	}
	private StringBuilder flip(int set) {
		if (rand.nextInt(2) == 0) {
			g.accept(this, g.left(set));
		} else {
			g.accept(this, g.right(set));
		}
		return buffer;
	}
	public StringBuilder set(int set) {
		boolean leftIsToken = g.get.token.compute(g.left(set));
		boolean rightIsToken = g.get.token.compute(g.right(set));
		// If neither (or both) sides are terminals, just pick a random side
		if (leftIsToken == rightIsToken) { return flip(set); }
		// Randomly pick the nonterminal side a limited number of times.
		int number = rand.nextInt(3) + 1;
		if (number <= 2) {
			depthLimit -= number;
		}
		if (!leftIsToken) {
			if (number <= 2 && depthLimit >= 0) {
				g.accept(this, g.left(set));
			}
			else {
				g.accept(this, g.right(set));
			}
		}
		else if (!rightIsToken) {
			if (number <= 2 && depthLimit >= 0) {
				g.accept(this, g.right(set));
			}
			else {
				g.accept(this, g.left(set));
			}
		}
//		System.out.format("left %s : token? %b\n", g.get.printer.compute(set.left), leftIsToken);
//		System.out.format("right %s : token? %b\n", g.get.printer.compute(set.right), rightIsToken);
		return buffer;
	}
	public StringBuilder id(int id) {
		if (g.get.nullable.compute(id)) return buffer;
		g.acceptRule(this, id);
		return buffer;
	}
	public StringBuilder rule(int rule) {
		g.accept(this, g.right(rule));
		return buffer;
	}
	public StringBuilder bottom() {
		return buffer;
	}
	public boolean done(StringBuilder accumulator) {
		return true;
	}
	public StringBuilder reduce(StringBuilder accumulator, StringBuilder current) {
		return buffer;
	}
	public void begin() {
		buffer = new StringBuilder();
	}
	public StringBuilder compute(int loopLimit, int recursionLimit) {
		this.depthLimit = recursionLimit;
		this.loopLimit = loopLimit;
		return super.compute();
	}
}
