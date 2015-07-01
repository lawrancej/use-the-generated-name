package languageV2.traversal;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.TaggedData;
import util.TaggedDataPair;

public class Printer extends AbstractVisitor<StringBuffer> {
	public Printer(Language g) {
		super(g);
	}
	private StringBuffer buffer = new StringBuffer();
	public StringBuffer symbol(TaggedData<Character> language) {
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
	public StringBuffer list(TaggedData<TaggedDataPair> language) {
		TaggedDataPair list = language.data;
		if (list == null) {
			buffer.append("\u03b5");
		} else {
			buffer.append('(');
			g.accept(this, list.left);
			buffer.append(' ');
//			buffer.append(list.right.hashCode());
			g.accept(this, list.right);
			buffer.append(')');
		}
		return buffer;
	}
	public StringBuffer set(TaggedData<SetOfLanguages> language) {
		SetOfLanguages set = language.data;
		if (set == null) {
			buffer.append("\u2205");
		} else {
			buffer.append('(');
			boolean flag = false;
//			buffer.append(set.hashCode());
			for (TaggedData<?> l : set) {
				if (flag) {
					buffer.append('|');
				} else {
					flag = true;
				}
				g.accept(this, l);
			}
			buffer.append(')');
		}
		return buffer;
	}
	public StringBuffer loop(TaggedData<TaggedData<?>> language) {
		TaggedData<?> loop = language.data;
		buffer.append('(');
		g.accept(this,loop);
		buffer.append(")*");
		return buffer;
	}
	public StringBuffer id(Language.Id id) {
		buffer.append('<');
		buffer.append(id.data);
		buffer.append('>');
		return buffer;
	}
	public StringBuffer rule(Language.Id id, TaggedData<?> rhs) {
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
