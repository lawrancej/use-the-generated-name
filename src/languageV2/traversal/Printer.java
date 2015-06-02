package languageV2.traversal;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.TaggedData;
import util.TaggedDataPair;

public class Printer extends AbstractVisitor<StringBuffer> {
	public Printer(Language g) {
		super(g, new WorkList<String>());
	}
	private StringBuffer buffer = new StringBuffer();
	public StringBuffer symbol(Character c) {
		if (c == null) {
			buffer.append("<any character>");
		} else {
			buffer.append('\'');
			buffer.append(c);
			buffer.append('\'');
		}
		return buffer;
	}
	public StringBuffer list(TaggedDataPair list) {
		if (list == null) {
			buffer.append("\u03b5");
		} else {
			buffer.append('(');
			g.visit(this, list.left);
			buffer.append(' ');
			g.visit(this, list.right);
			buffer.append(')');
		}
		return buffer;
	}
	public StringBuffer set(SetOfLanguages set) {
		if (set == null) {
			buffer.append("\u2205");
		} else {
			buffer.append('(');
			boolean flag = false;
			for (TaggedData<?> l : set) {
				if (flag) {
					buffer.append('|');
				} else {
					flag = true;
				}
				g.visit(this, l);
			}
			buffer.append(')');
		}
		return buffer;
	}
	public StringBuffer loop(TaggedData<?> loop) {
		buffer.append('(');
		g.visit(this,loop);
		buffer.append(")*");
		return buffer;
	}
	public StringBuffer id(String id) {
		buffer.append('<');
		buffer.append(id);
		buffer.append('>');
		return buffer;
	}
	public StringBuffer rule(String id, TaggedData<?> rhs) {
		this.id(id);
		buffer.append(" ::= ");
		g.visit(this, rhs);
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
