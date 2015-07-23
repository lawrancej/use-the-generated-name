package com.dictorobitary.traversal;

import java.util.Random;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;
import com.dictorobitary.Node;

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
	public StringBuilder any(Node<?, ?> language) {
		return buffer.append((char)(rand.nextInt(127)+1));
	}
	public StringBuilder symbol(Node<Character, Character> language) {
		if (language.left == language.right) {
			return buffer.append(language.left);
		} else {
			return buffer.append((char)(rand.nextInt(language.right - language.left) + language.left));
		}
	}
	public StringBuilder empty(Node<?, ?> language) {
		return buffer;
	}
	public StringBuilder list(Node<Node<?, ?>, Node<?, ?>> language) {
		Node.accept(this, language.left);
		Node.accept(this, language.right);
		return buffer;
	}
	public StringBuilder loop(Node<Node<?, ?>, Node<?, ?>> language) {
		for (int iterations = rand.nextInt(loopLimit); iterations > 0; iterations--) {
			Node.accept(this, language.left);
		}
		return buffer;
	}
	public StringBuilder reject(Node<?, ?> language) {
		return buffer;
	}
	private StringBuilder flip(Node<Node<?,?>, Node<?,?>> set) {
		if (rand.nextInt(2) == 0) {
			Node.accept(this, set.left);
		} else {
			Node.accept(this, set.right);
		}
		return buffer;
	}
	public StringBuilder set(Node<Node<?, ?>, Node<?, ?>> set) {
		boolean leftIsToken = g.get.token.compute(set.left);
		boolean rightIsToken = g.get.token.compute(set.right);
		// If neither (or both) sides are terminals, just pick a random side
		if (leftIsToken == rightIsToken) { return flip(set); }
		// Randomly pick the nonterminal side a limited number of times.
		int number = rand.nextInt(3) + 1;
		if (number <= 2) {
			depthLimit -= number;
		}
		if (!leftIsToken) {
			if (number <= 2 && depthLimit >= 0) {
				Node.accept(this, set.left);
			}
			else {
				Node.accept(this, set.right);
			}
		}
		else if (!rightIsToken) {
			if (number <= 2 && depthLimit >= 0) {
				Node.accept(this, set.right);
			}
			else {
				Node.accept(this, set.left);
			}
		}
//		System.out.format("left %s : token? %b\n", g.get.printer.compute(set.left), leftIsToken);
//		System.out.format("right %s : token? %b\n", g.get.printer.compute(set.right), rightIsToken);
		return buffer;
	}
	public StringBuilder id(Node<String, Void> id) {
		if (g.get.nullable.compute(id)) return buffer;
		g.acceptRule(this, id);
		return buffer;
	}
	public StringBuilder rule(Node<Node<String, Void>, Node<?, ?>> rule) {
		Node.accept(this, rule.right);
		return buffer;
	}
	public StringBuilder bottom() {
		return buffer;
	}
	public boolean done(StringBuilder accumulator) {
		return false;
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
