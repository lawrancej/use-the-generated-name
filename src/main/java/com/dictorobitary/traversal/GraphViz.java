package com.dictorobitary.traversal;

import java.util.HashSet;
import java.util.Set;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;

/**
 * Debug grammar through GraphViz output and also query the size of the graph.
 * @author Joseph Lawrance
 */
public class GraphViz extends AbstractVisitor<StringBuffer> {
	StringBuffer buffer;
	public GraphViz(Language g) {
		super(g);
	}
	private Set<Integer> nodes = new HashSet<Integer>();
	private Set<String> edges = new HashSet<String>();
	private String label(int language) {
		return "node" + Integer.toHexString(language);
	}
	private String label(long id) {
		return "node" + Long.toHexString(id);
	}
	private boolean drawNode(String name, int language) {
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
	public StringBuffer any(int language) {
		drawNode("Any", language);
		return buffer;
	}
	public StringBuffer symbol(int language) {
		if (g.left(language) == g.right(language)) {
			drawNode(String.format("'%c'", g.left(language)), language);
		} else {
			drawNode(String.format("'%c'..'%c'", g.left(language), g.right(language)), language);
		}
		return buffer;
	}
	@Override
	public StringBuffer empty(int language) {
		drawNode("&epsilon;", language);
		return buffer;
	}
	public StringBuffer list(int list) {
		if(drawNode("{List|{<left> L|<right> R}}", list)) {
			g.accept(this, g.left(list));
			int r = g.left(list);
			drawEdge(label(list) + ":left", r);
			g.accept(this, g.right(list));
			int r1 = g.right(list);
			drawEdge(label(list) + ":right", r1);
		}
		return buffer;
	}
	public StringBuffer loop(int language) {
		if (drawNode("Loop", language)) {
			g.accept(this, g.left(language));
			int r = g.left(language);
			drawEdge(label(language), r);
		}
		return buffer;
	}
	public StringBuffer reject(int language) {
		drawNode("Reject", language);
		return buffer;
	}
	public StringBuffer set(int set) {
		if (drawNode("Set", set)) {
			g.accept(this, g.left(set));
			int r = g.left(set);
			drawEdge(label(set), r);
			g.accept(this, g.right(set));
			int r1 = g.right(set);
			drawEdge(label(set), r1);
		}
		return buffer;
	}
	public StringBuffer id(int id) {
		if (g.left(id) == 0) {
			drawNode("Id", id);
		}
		else {
			drawNode(String.format("Id '%s'", g.left(id)), id);
		}
		return buffer;
	}
	public StringBuffer rule(int rule) {
		this.id(g.left(rule));
		g.accept(this, g.right(rule));
		int r = g.right(rule);
		drawEdge(label(g.left(rule)), r);
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
