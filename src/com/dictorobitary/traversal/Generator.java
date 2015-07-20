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
	public StringBuilder reject(Node<?, ?> langauge) {
		return null;
	}
	public StringBuilder set(Node<Node<?, ?>, Node<?, ?>> set) {
		if (rand.nextInt(2) == 0) {
			Node.accept(this, set.left);
		} else {
			Node.accept(this, set.right);
		}
		return buffer;
	}
	public StringBuilder id(Node<String, Void> id) {
		if (g.get.nullable.compute(id)) {
			if (todo.visited(id)) {
				return buffer;
			}
			if (rand.nextInt(2) == 0) {
				g.acceptRule(this, id);				
			}
		} else {
			g.acceptRule(this, id);
		}
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
}
