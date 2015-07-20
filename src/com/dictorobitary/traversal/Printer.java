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
		if (language.left == language.right) {
			buffer.append('\'');
			buffer.append(language.left);
			buffer.append('\'');
		} else {
			buffer.append('[');
			buffer.append(language.left);
			buffer.append('-');
			buffer.append(language.right);
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
		Node.accept(this, list.left);
		buffer.append(' ');
		Node.accept(this, list.right);
		buffer.append(')');
		return buffer;
	}
	public StringBuffer reject(Node<?, ?> language) {
		buffer.append("\u2205");
		return buffer;
	}
	public StringBuffer set(Node<Node<?,?>,Node<?,?>> set) {
		buffer.append('(');
		Node.accept(this, set.left);
		buffer.append('|');
		Node.accept(this, set.right);
		buffer.append(')');
		return buffer;
	}
	public StringBuffer id(Node<String,Void> id) {
		buffer.append('<');
		if (id.left == null) {
			buffer.append(id.hashCode());
		} else {
			buffer.append(id.left);
		}
		buffer.append('>');
		return buffer;
	}
	public StringBuffer rule(Node<Node<String,Void>,Node<?,?>> rule) {
		this.id(rule.left);
		buffer.append(" ::= ");
		Node.accept(this, rule.right);
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
