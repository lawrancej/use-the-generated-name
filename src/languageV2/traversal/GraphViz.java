package languageV2.traversal;

import util.TaggedData;
import util.TaggedDataPair;
import languageV2.Language;
import languageV2.Language.Id;
import languageV2.SetOfLanguages;

public class GraphViz extends AbstractVisitor<StringBuffer> {
	StringBuffer buffer;
	public GraphViz(Language g) {
		super(g);
	}
	public StringBuffer symbol(TaggedData<Character> language) {
		Character c = language.data;
		buffer.append(String.format("%s [label=\"Lit '%c'\"];\n", language.hashCode(), c));
		return buffer;
	}
	public StringBuffer list(TaggedData<TaggedDataPair> language) {
		TaggedDataPair list = language.data;
		if (list == null) {
			buffer.append(String.format("%s [label=\"Epsilon\"];\n", language.hashCode()));
			return buffer;
		}
		buffer.append(String.format("%s [label=\"Concat\"];\n", language.hashCode()));
		g.visit(this, list.left);
		buffer.append(String.format("%s -> %s [label=\"First\"];\n", language.hashCode(), list.left.hashCode()));
		g.visit(this, list.right);
		buffer.append(String.format("%s -> %s [label=\"Second\"];\n", language.hashCode(), list.right.hashCode()));
		return buffer;
	}
	public StringBuffer loop(TaggedData<TaggedData<?>> language) {
		buffer.append(String.format("%s [label=\"Loop\"];\n", language.hashCode()));
		buffer.append(String.format("%s -> %s;\n", language.hashCode(), language.data.hashCode()));
		return buffer;
	}
	public StringBuffer set(TaggedData<SetOfLanguages> language) {
		SetOfLanguages set = language.data;
		if (set == null) {
			buffer.append(String.format("%s [label=\"Reject\"];\n", language.hashCode()));
			return buffer;
		}
		buffer.append(String.format("%s [label=\"Alternative\"];\n", language.hashCode()));
		for (TaggedData<?> l : set) {
			g.visit(this, l);
			buffer.append(String.format("%s -> %s;\n", language.hashCode(), l.hashCode()));
		}
		return buffer;
	}
	public StringBuffer id(Id id) {
		buffer.append(String.format("%s [label=\"Recurrence\"];\n", id.hashCode()));
		return buffer;
	}
	public StringBuffer rule(Id id, TaggedData<?> rhs) {
		buffer.append(String.format("%s [label=\"Recurrence\"];\n", id.hashCode()));
		g.visit(this, rhs);
		buffer.append(String.format("%s -> %s;\n", id.hashCode(), rhs.hashCode()));
		return buffer;
	}
	public StringBuffer bottom() {
		return buffer;
	}
	public boolean done(StringBuffer accumulator) {
		// TODO Auto-generated method stub
		return false;
	}
	public StringBuffer reduce(StringBuffer accumulator, StringBuffer current) {
		return buffer;
	}
	public void begin() {
		buffer = new StringBuffer();
		buffer.append("digraph f { ");
	}
	public void end() {
		buffer.append(" }");
	}

}
