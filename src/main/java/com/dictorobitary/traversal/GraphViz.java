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
		return "node" + Long.toHexString(Node.id(language));
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
		if (Node.left(language) == Node.right(language)) {
			drawNode(String.format("'%c'", Node.left(language)), language);
		} else {
			drawNode(String.format("'%c'..'%c'", Node.left(language), Node.right(language)), language);
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
			Node.accept(this, Node.left(list));
			Node<?, ?> r = Node.left(list);
			drawEdge(label(list) + ":left", Node.id(r));
			Node.accept(this, Node.right(list));
			Node<?, ?> r1 = Node.right(list);
			drawEdge(label(list) + ":right", Node.id(r1));
		}
		return buffer;
	}
	public StringBuffer loop(Node<Node<?,?>,Node<?,?>> language) {
		if (drawNode("Loop", language)) {
			Node.accept(this, Node.left(language));
			Node<?, ?> r = Node.left(language);
			drawEdge(label(language), Node.id(r));
		}
		return buffer;
	}
	public StringBuffer reject(Node<?, ?> language) {
		drawNode("Reject", language);
		return buffer;
	}
	public StringBuffer set(Node<Node<?,?>,Node<?,?>> set) {
		if (drawNode("Set", set)) {
			Node.accept(this, Node.left(set));
			Node<?, ?> r = Node.left(set);
			drawEdge(label(set), Node.id(r));
			Node.accept(this, Node.right(set));
			Node<?, ?> r1 = Node.right(set);
			drawEdge(label(set), Node.id(r1));
		}
		return buffer;
	}
	public StringBuffer id(Node<String,Void> id) {
		if (Node.left(id) == null) {
			drawNode("Id", id);
		}
		else {
			drawNode(String.format("Id '%s'", Node.left(id)), id);
		}
		return buffer;
	}
	public StringBuffer rule(Node<Node<String,Void>,Node<?,?>> rule) {
		this.id(Node.left(rule));
		Node.accept(this, Node.right(rule));
		Node<?, ?> r = Node.right(rule);
		drawEdge(label(Node.left(rule)), Node.id(r));
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
