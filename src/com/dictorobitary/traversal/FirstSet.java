package com.dictorobitary.traversal;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;
import com.dictorobitary.Node;

// Compute the first set
public class FirstSet extends AbstractVisitor<Node<?,?>> {
	public FirstSet(Language g) {
		super(g);
	}
	public Node<?, ?> any(Node<?, ?> language) {
		return language;
	}
	public Node<?,?> symbol(Node<Character,Character> language) {
		return language;
	}
	public Node<?, ?> empty(Node<?, ?> language) {
		return bottom();
	}
	public Node<?,?> list(Node<Node<?,?>,Node<?,?>> pair) {
		Node<?,?> result = Node.accept(this, pair.left);
		if (g.get.nullable.compute(pair.left)) {
			result = g.or(result, Node.accept(this, pair.right));
		}
		return result;
	}
	public Node<?, ?> reject(Node<?, ?> langauge) {
		return bottom();
	}
	public Node<?,?> set(Node<Node<?,?>,Node<?,?>> set) {
		return g.or(Node.accept(this, set.left), Node.accept(this, set.right));
	}
	public Node<?,?> id(Node<String,Void> id) {
		return bottom();
	}
	public Node<?,?> rule(Node<Node<String,Void>,Node<?,?>> rule) {
		return Node.accept(this, rule.right);
	}
	public Node<?,?> bottom() {
		return Language.reject;
	}
	public Node<?,?> reduce(Node<?,?> accumulator, Node<?,?> current) {
		return g.or(accumulator, current);
	}
	public boolean done(Node<?,?> accumulator) {
		return false;
	}
}
