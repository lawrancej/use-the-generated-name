package languageV2.traversal;

import languageV2.Language;
import languageV2.Node;

public class Printer extends AbstractVisitor<StringBuffer> {
	public Printer(Language g) {
		super(g);
	}
	private StringBuffer buffer = new StringBuffer();
	public StringBuffer symbol(Node<Character,Character> language) {
		if (language == Language.any) {
			buffer.append("<any character>");
		} else if (language.left == language.right) {
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
	public StringBuffer list(Node<Node<?,?>,Node<?,?>> list) {
		if (list == Language.empty) {
			buffer.append("\u03b5");
		} else {
			buffer.append('(');
			g.accept(this, list.left);
			buffer.append(' ');
			g.accept(this, list.right);
			buffer.append(')');
		}
		return buffer;
	}
	public StringBuffer set(Node<Node<?,?>,Node<?,?>> set) {
		if (set == Language.reject) {
			buffer.append("\u2205");
		} else {
			buffer.append('(');
			g.accept(this, set.left);
			buffer.append('|');
			g.accept(this, set.right);
			buffer.append(')');
		}
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
		g.accept(this, rule.right);
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
