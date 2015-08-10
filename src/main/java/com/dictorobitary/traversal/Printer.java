package com.dictorobitary.traversal;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;
import com.dictorobitary.Node;

public class Printer extends AbstractVisitor<StringBuffer> {
	public Printer(Language g) {
		super(g);
	}
	private StringBuffer buffer = new StringBuffer();
	public StringBuffer any(Node<?, ?> language) {
		buffer.append("<any character>");
		return buffer;
	}
	public StringBuffer symbol(Node<Character,Character> language) {
		if (Node.left(language) == Node.right(language)) {
			buffer.append('\'');
			buffer.append(Node.left(language));
			buffer.append('\'');
		} else {
			buffer.append('[');
			buffer.append(Node.left(language));
			buffer.append('-');
			buffer.append(Node.right(language));
			buffer.append(']');
		}
		return buffer;
	}
	public StringBuffer empty(Node<?, ?> language) {
		buffer.append("\u03b5");
		return buffer;
	}
	public StringBuffer list(Node<Node<?,?>,Node<?,?>> list) {
		buffer.append('(');
		Node.accept(this, Node.left(list));
		buffer.append(' ');
		Node.accept(this, Node.right(list));
		buffer.append(')');
		return buffer;
	}
	public StringBuffer loop(Node<Node<?,?>,Node<?,?>> loop) {
		buffer.append('(');
		Node.accept(this,Node.left(loop));
		if (Node.right(loop) == Language.any) {
			buffer.append(")*");
		} else {
			// FIXME
			buffer.append("){FIXME}");
		}
		return buffer;
	}
	public StringBuffer reject(Node<?, ?> language) {
		buffer.append("\u2205");
		return buffer;
	}
	public StringBuffer set(Node<Node<?,?>,Node<?,?>> set) {
		buffer.append('(');
		Node.accept(this, Node.left(set));
		buffer.append('|');
		Node.accept(this, Node.right(set));
		buffer.append(')');
		return buffer;
	}
	public StringBuffer id(Node<String,Void> id) {
		buffer.append('<');
		if (Node.left(id) == null) {
			buffer.append(id.hashCode());
		} else {
			buffer.append(Node.left(id));
		}
		buffer.append('>');
		return buffer;
	}
	public StringBuffer rule(Node<Node<String,Void>,Node<?,?>> rule) {
		this.id(Node.left(rule));
		buffer.append(" ::= ");
		Node.accept(this, Node.right(rule));
		buffer.append("\n");
		return buffer;
	}
	public StringBuffer bottom() {
		return buffer;
	}
	public StringBuffer reduce(StringBuffer accumulator, StringBuffer current) {
		return buffer;
	}
	public boolean done(StringBuffer accumulator) {
		return false;
	}
	public void begin() {
		buffer = new StringBuffer();
	}
}
