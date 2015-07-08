package languageV2.traversal;

import java.util.HashSet;
import java.util.Set;

import util.Node;
import util.TaggedDataPair;
import languageV2.Language;
import languageV2.Language.Id;

public class GraphViz extends AbstractVisitor<StringBuffer> {
	StringBuffer buffer;
	public GraphViz(Language g) {
		super(g);
	}
	private Set<String> arrows;
	private void drawArrow(Object from, int to) {
		String arrow = String.format("%s -> %s;\n", from, to);
		if (!arrows.contains(arrow)) {
			arrows.add(arrow);
			buffer.append(arrow);
		}
	}
	public StringBuffer symbol(Node<Character> language) {
		Character c = language.data;
		buffer.append(String.format("%s [label=\"'%c'\"];\n", language.hashCode(), c));
		return buffer;
	}
	public StringBuffer list(Node<TaggedDataPair> language) {
		TaggedDataPair list = language.data;
		if (list == null) {
			buffer.append(String.format("%s [label=\"&epsilon;\"];\n", language.hashCode()));
			return buffer;
		}
		buffer.append(String.format("%s [label=\"{List|{<left> L|<right> R}}\"];\n", language.hashCode()));
		g.accept(this, list.left);
		drawArrow(language.hashCode() + ":left", list.left.hashCode());
		g.accept(this, list.right);
		drawArrow(language.hashCode() + ":right", list.right.hashCode());
		return buffer;
	}
	public StringBuffer loop(Node<Node<?>> language) {
		buffer.append(String.format("%s [label=\"Loop\"];\n", language.hashCode()));
		g.accept(this, language.data);
		drawArrow(language.hashCode(), language.data.hashCode());
		return buffer;
	}
	public StringBuffer set(Node<TaggedDataPair> language) {
		TaggedDataPair set = language.data;
		if (set == null) {
			buffer.append(String.format("%s [label=\"Reject\"];\n", language.hashCode()));
			return buffer;
		}
		buffer.append(String.format("%s [label=\"Set\"];\n", language.hashCode()));
		g.accept(this, set.left);
		drawArrow(language.hashCode(), set.left.hashCode());
		g.accept(this, set.right);
		drawArrow(language.hashCode(), set.right.hashCode());
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
	public StringBuffer rule(Id id, Node<?> rhs) {
		this.id(id);
		g.accept(this, rhs);
		drawArrow(id.hashCode(), rhs.hashCode());
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
		arrows = new HashSet<String>();
		buffer = new StringBuffer();
		buffer.append("digraph f { \n");
		buffer.append("node [shape=record];\n");
	}
	public void end() {
		buffer.append(" }");
	}
}
