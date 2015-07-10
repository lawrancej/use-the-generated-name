package languageV2.traversal;

import java.util.HashSet;
import java.util.Set;

import languageV2.Language;
import languageV2.Node;
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
	public StringBuffer symbol(Node<Character,Character> language) {
		if (language == Language.any) {
			buffer.append(String.format("%s [label=\"Any\"];\n", language.hashCode()));
		} else {
			buffer.append(String.format("%s [label=\"'%c'\"];\n", language.hashCode(), language.left));
		}
		return buffer;
	}
	public StringBuffer list(Node<Node<?,?>,Node<?,?>> list) {
		if (list == Language.empty) {
			buffer.append(String.format("%s [label=\"&epsilon;\"];\n", list.hashCode()));
			return buffer;
		}
		buffer.append(String.format("%s [label=\"{List|{<left> L|<right> R}}\"];\n", list.hashCode()));
		g.accept(this, list.left);
		drawArrow(list.hashCode() + ":left", list.left.hashCode());
		g.accept(this, list.right);
		drawArrow(list.hashCode() + ":right", list.right.hashCode());
		return buffer;
	}
	public StringBuffer loop(Node<Node<?,?>,Node<?,?>> language) {
		buffer.append(String.format("%s [label=\"Loop\"];\n", language.hashCode()));
		g.accept(this, language.left);
		drawArrow(language.hashCode(), language.left.hashCode());
		return buffer;
	}
	public StringBuffer set(Node<Node<?,?>,Node<?,?>> set) {
		if (set == Language.reject) {
			buffer.append(String.format("%s [label=\"Reject\"];\n", set.hashCode()));
			return buffer;
		}
		buffer.append(String.format("%s [label=\"Set\"];\n", set.hashCode()));
		g.accept(this, set.left);
		drawArrow(set.hashCode(), set.left.hashCode());
		g.accept(this, set.right);
		drawArrow(set.hashCode(), set.right.hashCode());
		return buffer;
	}
	public StringBuffer id(Id id) {
		if (id.left == null) {
			buffer.append(String.format("%s [label=\"Id\"];\n", id.hashCode()));
		} else {
			buffer.append(String.format("%s [label=\"Id '%s'\"];\n", id.hashCode(), id.left));
		}
		return buffer;
	}
	public StringBuffer rule(Id id, Node<?,?> rhs) {
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
	@Override
	public StringBuffer end(StringBuffer accumulator) {
		buffer.append(" }");
		return accumulator;
	}
}
