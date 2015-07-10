package languageV2.traversal;

import languageV2.Language;
import util.Node;

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
			buffer.append(language.right);
			buffer.append(']');
		}
		return buffer;
	}
	public StringBuffer list(Node<Node<?,?>,Node<?,?>> list) {
		if (list == null) {
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
		if (set == null) {
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
	public StringBuffer loop(Node<Node<?,?>,Node<?,?>> loop) {
		buffer.append('(');
		g.accept(this,loop.left);
		buffer.append(")*");
		return buffer;
	}
	public StringBuffer id(Language.Id id) {
		buffer.append('<');
		if (id.left == null) {
			buffer.append(id.hashCode());
		} else {
			buffer.append(id.left);
		}
		buffer.append('>');
		return buffer;
	}
	public StringBuffer rule(Language.Id id, Node<?,?> rhs) {
		this.id(id);
		buffer.append(" ::= ");
		g.accept(this, rhs);
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
	@Override
	public StringBuffer end(StringBuffer accumulator) {
		return accumulator;
	}
}
