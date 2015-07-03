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
		buffer.append(String.format("%s [label=\"'%c'\"];\n", language.hashCode(), c));
		return buffer;
	}
	public StringBuffer list(TaggedData<TaggedDataPair> language) {
		TaggedDataPair list = language.data;
		if (list == null) {
			buffer.append(String.format("%s [label=\"&epsilon;\"];\n", language.hashCode()));
			return buffer;
		}
		buffer.append(String.format("%s [label=\"{List|{<left> L|<right> R}}\"];\n", language.hashCode()));
		g.accept(this, list.left);
		buffer.append(String.format("%s:left -> %s;\n", language.hashCode(), list.left.hashCode()));
		g.accept(this, list.right);
		buffer.append(String.format("%s:right -> %s;\n", language.hashCode(), list.right.hashCode()));
		return buffer;
	}
	public StringBuffer loop(TaggedData<TaggedData<?>> language) {
		buffer.append(String.format("%s [label=\"Loop\"];\n", language.hashCode()));
		g.accept(this, language.data);
		buffer.append(String.format("%s -> %s;\n", language.hashCode(), language.data.hashCode()));
		return buffer;
	}
	public StringBuffer set(TaggedData<SetOfLanguages> language) {
		SetOfLanguages set = language.data;
		if (set == null) {
			buffer.append(String.format("%s [label=\"Reject\"];\n", language.hashCode()));
			return buffer;
		}
		buffer.append(String.format("%s [label=\"Set\"];\n", language.hashCode()));
		for (TaggedData<?> l : set) {
			g.accept(this, l);
			buffer.append(String.format("%s -> %s;\n", language.hashCode(), l.hashCode()));
		}
		return buffer;
	}
	public StringBuffer id(Id id) {
		if (id.data == null) {
			buffer.append(String.format("%s [label=\"Id\"];\n", id.hashCode()));
		} else {
			buffer.append(String.format("%s [label=\"Id '%s'\"];\n", id.hashCode(), id.data));
		}
		return buffer;
	}
	public StringBuffer rule(Id id, TaggedData<?> rhs) {
		this.id(id);
		g.accept(this, rhs);
		buffer.append(String.format("%s -> %s;\n", id.hashCode(), rhs.hashCode()));
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
		buffer = new StringBuffer();
		buffer.append("digraph f { \n");
		buffer.append("node [shape=record];\n");
	}
	public void end() {
		buffer.append(" }");
	}
}
