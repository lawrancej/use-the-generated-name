package com.dictorobitary.traversal;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;
import com.dictorobitary.Node;

public class Derivative extends AbstractVisitor<Node<?,?>> {
	public Character c;
	private Map<Node<String,Void>, Node<String,Void>> ids = new LinkedHashMap<Node<String,Void>, Node<String,Void>>();
	public Derivative(Language g) {
		super(g);
	}
	public Node<?, ?> any(Node<?, ?> language) {
		// D(.) = e
		return Language.empty;
	}
	public Node<?,?> symbol(Node<Character,Character> language) {
		// Dc(c) = e
		if (this.c == language.left || (this.c > language.left && this.c <= language.right)) {
			return Language.empty;
		}
		// Dc(c') = 0
		return bottom();
	}
	public Node<?, ?> empty(Node<?, ?> language) {
		// Dc(e) = 0
		return bottom();
	}
	public Node<?,?> list(Node<Node<?,?>,Node<?,?>> list) {
		// Dc(ab) = Dc(a)b + nullable(a)Dc(b)
		Node<?,?> result = g.list(Node.accept(this, list.left), list.right);
		if (g.get.nullable.compute(list.left)) {
			return g.or(result, Node.accept(this, list.right));
		}
		return result;
	}
	public Node<?, ?> loop(Node<Node<?, ?>, Node<?, ?>> language) {
		// Dc(a*) = Dc(a)a* = Dc(aa*)
//		return Node.accept(this, g.list(language.left, language));
		return g.list(Node.accept(this, language.left), language);
	}
	public Node<?, ?> reject(Node<?, ?> langauge) {
		// Dc(0) = 0
		return bottom();
	}
	public Node<?,?> set(Node<Node<?,?>,Node<?,?>> set) {
		// Dc(a+b) = Dc(a) + Dc(b)
		return g.or(Node.accept(this, set.left), Node.accept(this, set.right));
	}
	private Node<String,Void> getReplacement(Node<String,Void> id) {
		if (!ids.containsKey(id)) {
			Node<String,Void> replacement = g.id();
			ids.put(id, replacement);
			return replacement;
		}
		else {
			return ids.get(id);
		}
	}
	public Node<?,?> id(Node<String,Void> id) {
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
	public Node<?,?> rule(Node<Node<String,Void>, Node<?,?>> rule) {
		// By this point, we've seen the identifier on the rhs before.
		// If the identifier derives a non-empty set, return the identifier
		if (ids.containsKey(rule.left)) {
			return ids.get(rule.left);
		}
		// Visit the rhs
		Node<?,?> derivation = Node.accept(this,  rule.right);
		
		// Don't create a rule that rejects or is empty
		if (derivation == Language.reject || derivation == Language.empty) {
			return derivation;
		}
		// Create a new rule
		return g.rule(getReplacement(rule.left), derivation);
	}
	public Node<?,?> bottom() {
		return Language.reject;
	}
	public Node<?,?> reduce(Node<?,?> accumulator, Node<?,?> current) {
		if (accumulator == bottom()) return current;
		else return accumulator;
	}
	public boolean done(Node<?,?> accumulator) {
		return true;
	}
	public void begin() {
		ids.clear();
	}
}
