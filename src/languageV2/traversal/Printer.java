package languageV2.traversal;

import languageV2.Language;
import util.Node;
import util.TaggedDataPair;

public class Printer extends AbstractVisitor<StringBuffer> {
	public Printer(Language g) {
		super(g);
	}
	private StringBuffer buffer = new StringBuffer();
	public StringBuffer symbol(Node<Character> language) {
		Character c = language.data;
		if (c == null) {
			buffer.append("<any character>");
		} else {
			buffer.append('\'');
			buffer.append(c);
			buffer.append('\'');
		}
		return buffer;
	}
	public StringBuffer list(Node<TaggedDataPair> language) {
		TaggedDataPair list = language.data;
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
	public StringBuffer set(Node<TaggedDataPair> language) {
		TaggedDataPair set = language.data;
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
	public StringBuffer loop(Node<Node<?>> language) {
		Node<?> loop = language.data;
		buffer.append('(');
		g.accept(this,loop);
		buffer.append(")*");
		return buffer;
	}
	public StringBuffer id(Language.Id id) {
		buffer.append('<');
		if (id.data == null) {
			buffer.append(id.hashCode());
		} else {
			buffer.append(id.data);
		}
		buffer.append('>');
		return buffer;
	}
	public StringBuffer rule(Language.Id id, Node<?> rhs) {
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
}
