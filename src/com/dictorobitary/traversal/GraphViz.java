package com.dictorobitary.traversal;

import java.util.HashSet;
import java.util.Set;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;
import com.dictorobitary.Node;

/**
 * Debug grammar through GraphViz output and also query the size of the graph.
 * @author Joseph Lawrance
 */
public class GraphViz extends AbstractVisitor<StringBuffer> {
	StringBuffer buffer;
	public GraphViz(Language g) {
		super(g);
	}
	private Set<Node<?,?>> nodes = new HashSet<Node<?,?>>();
	private Set<String> edges = new HashSet<String>();
	private String label(Node<?,?> language) {
		return "node" + Long.toHexString(language.id);
	}
	private String label(long id) {
		return "node" + Long.toHexString(id);
	}
	private boolean drawNode(String name, Node<?,?> language) {
		if (!nodes.contains(language)) {
			nodes.add(language);
			buffer.append(String.format("%s [label=\"%s\"];\n", label(language), name));
			return true;
		}
		return false;
	}
	private void drawEdge(Object from, long to) {
		String arrow = String.format("%s -> %s;\n", from, label(to));
		if (!edges.contains(arrow)) {
			edges.add(arrow);
			buffer.append(arrow);
		}
	}
	public StringBuffer any(Node<?, ?> language) {
		drawNode("Any", language);
		return buffer;
	}
	public StringBuffer symbol(Node<Character,Character> language) {
		if (language.left == language.right) {
			drawNode(String.format("'%c'", language.left), language);
		} else {
			drawNode(String.format("'%c'..'%c'", language.left, language.right), language);
		}
		return buffer;
	}
	@Override
	public StringBuffer empty(Node<?, ?> language) {
		drawNode("&epsilon;", language);
		return buffer;
	}
	public StringBuffer list(Node<Node<?,?>,Node<?,?>> list) {
		if(drawNode("{List|{<left> L|<right> R}}", list)) {
			Node.accept(this, list.left);
			drawEdge(label(list) + ":left", list.left.id);
			Node.accept(this, list.right);
			drawEdge(label(list) + ":right", list.right.id);
		}
		return buffer;
	}
	public StringBuffer loop(Node<Node<?,?>,Node<?,?>> language) {
		if (drawNode("Loop", language)) {
			Node.accept(this, language.left);
			drawEdge(label(language), language.left.id);
		}
		return buffer;
	}
	public StringBuffer reject(Node<?, ?> language) {
		drawNode("Reject", language);
		return buffer;
	}
	public StringBuffer set(Node<Node<?,?>,Node<?,?>> set) {
		if (drawNode("Set", set)) {
			Node.accept(this, set.left);
			drawEdge(label(set), set.left.id);
			Node.accept(this, set.right);
			drawEdge(label(set), set.right.id);
		}
		return buffer;
	}
	public StringBuffer id(Node<String,Void> id) {
		if (id.left == null) {
			drawNode("Id", id);
		}
		else {
			drawNode(String.format("Id '%s'", id.left), id);
		}
		return buffer;
	}
	public StringBuffer rule(Node<Node<String,Void>,Node<?,?>> rule) {
		this.id(rule.left);
		Node.accept(this, rule.right);
		drawEdge(label(rule.left), rule.right.id);
		return buffer;
	}
	public StringBuffer bottom() {
		return buffer;
	}
	public boolean done(StringBuffer accumulator) {
		return false;
	}
	public StringBuffer reduce(StringBuffer accumulator, StringBuffer current) {
		return buffer;
	}
	public void begin() {
		edges.clear();
		nodes.clear();
		buffer = new StringBuffer();
		buffer.append("digraph f { \n");
		buffer.append("node [shape=record];\n");
	}
	public StringBuffer end(StringBuffer accumulator) {
		buffer.append(" }");
		return accumulator;
	}
	public int edges() {
		return edges.size();
	}
	public int nodes() {
		return nodes.size();
	}
}
