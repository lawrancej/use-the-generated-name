package com.dictorobitary.traversal;

import com.dictorobitary.AbstractVisitor;
import com.dictorobitary.Language;

public class Printer extends AbstractVisitor<StringBuffer> {
	public Printer(Language g) {
		super(g);
	}
	private StringBuffer buffer = new StringBuffer();
	public StringBuffer any(int language) {
		buffer.append("<any character>");
		return buffer;
	}
	public StringBuffer symbol(int language) {
		if (g.left(language) == g.right(language)) {
			buffer.append('\'');
			buffer.append(g.left(language));
			buffer.append('\'');
		} else {
			buffer.append('[');
			buffer.append(g.left(language));
			buffer.append('-');
			buffer.append(g.right(language));
			buffer.append(']');
		}
		return buffer;
	}
	public StringBuffer empty(int language) {
		buffer.append("\u03b5");
		return buffer;
	}
	public StringBuffer list(int list) {
		buffer.append('(');
		g.accept(this, g.left(list));
		buffer.append(' ');
		g.accept(this, g.right(list));
		buffer.append(')');
		return buffer;
	}
	public StringBuffer loop(int loop) {
		buffer.append('(');
		g.accept(this,g.left(loop));
		if (g.right(loop) == g.any) {
			buffer.append(")*");
		} else {
			// FIXME
			buffer.append("){FIXME}");
		}
		return buffer;
	}
	public StringBuffer reject(int language) {
		buffer.append("\u2205");
		return buffer;
	}
	public StringBuffer set(int set) {
		buffer.append('(');
		g.accept(this, g.left(set));
		buffer.append('|');
		g.accept(this, g.right(set));
		buffer.append(')');
		return buffer;
	}
	public StringBuffer id(int id) {
		buffer.append('<');
		if (g.left(id) == 0) {
			buffer.append(id);
		} else {
			buffer.append(g.left(id));
		}
		buffer.append('>');
		return buffer;
	}
	public StringBuffer rule(int rule) {
		this.id(g.left(rule));
		buffer.append(" ::= ");
		g.accept(this, g.right(rule));
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
