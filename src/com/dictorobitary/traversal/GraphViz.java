package com.dictorobitary.traversal;

import java.util.HashSet;
import java.util.Set;

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
	private void drawEdge(Object from, int to) {
		String arrow = String.format("%s -> %s;\n", from, to);
		if (!edges.contains(arrow)) {
			edges.add(arrow);
			buffer.append(arrow);
		}
	}
	public StringBuffer symbol(Node<Character,Character> language) {
		if (!nodes.contains(language)) {
			nodes.add(language);
			if (language == Language.any) {
				buffer.append(String.format("%s [label=\"Any\"];\n", language.hashCode()));
			} else if (language.left == language.right) {
				buffer.append(String.format("%s [label=\"'%c'\"];\n", language.hashCode(), language.left));
			} else {
				buffer.append(String.format("%s [label=\"'%c'..'%c'\"];\n", language.hashCode(), language.left, language.right));
			}
		}
		return buffer;
	}
	public StringBuffer list(Node<Node<?,?>,Node<?,?>> list) {
		if (!nodes.contains(list)) {
			nodes.add(list);
			if (list == Language.empty) {
				buffer.append(String.format("%s [label=\"&epsilon;\"];\n", list.hashCode()));
				return buffer;
			}
			buffer.append(String.format("%s [label=\"{List|{<left> L|<right> R}}\"];\n", list.hashCode()));
			Node.accept(this, list.left);
			drawEdge(list.hashCode() + ":left", list.left.hashCode());
			Node.accept(this, list.right);
			drawEdge(list.hashCode() + ":right", list.right.hashCode());
		}
		return buffer;
	}
	public StringBuffer set(Node<Node<?,?>,Node<?,?>> set) {
		if (!nodes.contains(set)) {
			nodes.add(set);
			if (set == Language.reject) {
				buffer.append(String.format("%s [label=\"Reject\"];\n", set.hashCode()));
				return buffer;
			}
			buffer.append(String.format("%s [label=\"Set\"];\n", set.hashCode()));
			Node.accept(this, set.left);
			drawEdge(set.hashCode(), set.left.hashCode());
			Node.accept(this, set.right);
			drawEdge(set.hashCode(), set.right.hashCode());
		}
		return buffer;
	}
	public StringBuffer id(Node<String,Void> id) {
		if (!nodes.contains(id)) {
			nodes.add(id);
			if (id.left == null) {
				buffer.append(String.format("%s [label=\"Id\"];\n", id.hashCode()));
			} else {
				buffer.append(String.format("%s [label=\"Id '%s'\"];\n", id.hashCode(), id.left));
			}
		}
		return buffer;
	}
	public StringBuffer rule(Node<Node<String,Void>,Node<?,?>> rule) {
		this.id(rule.left);
		Node.accept(this, rule.right);
		drawEdge(rule.left.hashCode(), rule.right.hashCode());
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
