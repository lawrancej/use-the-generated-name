package com.dictorobitary.traversal;

import java.util.HashMap;
import java.util.Map;

import com.dictorobitary.Language;
import com.dictorobitary.Node;

public class Derivative extends AbstractVisitor<Node<?,?>> {
	public Character c;
	private Map<Node<String,Void>, Node<String,Void>> ids = new HashMap<Node<String,Void>, Node<String,Void>>();
	public Derivative(Language g) {
		super(g);
	}
	public Node<?,?> symbol(Node<Character,Character> language) {
		// Dc(c|.) = e
		if (language == Language.any || this.c == language.left || (this.c > language.left && this.c <= language.right)) {
			return Language.empty;
		}
		// Dc(c') = 0
		return bottom();
	}
	public Node<?,?> list(Node<Node<?,?>,Node<?,?>> list) {
		// Dc(e) = 0
		if (list == Language.empty) return bottom();
		// Dc(ab) = Dc(a)b + nullable(a)Dc(b)
		Node<?,?> result = g.list(Node.accept(this, list.left), list.right);
		if (g.get.nullable.compute(list.left)) {
			return g.or(result, Node.accept(this, list.right));
		}
		return result;
	}
	public Node<?,?> set(Node<Node<?,?>,Node<?,?>> set) {
		// Dc(0) = 0
		if (set == Language.reject) return bottom();
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
		// Handle left-recursion: return DcId if we're visiting Id -> Id
		if (todo.visiting(id)) {
			return getReplacement(id);
		}
		// Visit rule Id -> rhs, if we haven't already visited it.
		if (!todo.visited(id)) {
			return g.acceptRule(this, id);
		}
		// By this point, we've seen the identifier on the rhs before.
		// If the identifier derives a non-empty set, return the identifier
		if (ids.containsKey(id)) {
			return ids.get(id);
		}
		// Otherwise, return the empty set
		return bottom();
	}
	public Node<?,?> rule(Node<Node<String,Void>, Node<?,?>> rule) {
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
		return false;
	}
	public void begin() {
		ids.clear();
	}
}
