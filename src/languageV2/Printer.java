package languageV2;

public class Printer implements Visitor<StringBuffer> {
	Grammar g;
	StringBuffer buffer = new StringBuffer();
	WorkList<String> todo;
	public StringBuffer symbol(Character c) {
		buffer.append('\'');
		if (c == null) {
			buffer.append("<any character>");
		} else {
			buffer.append(c);
		}
		buffer.append('\'');
		return buffer;
	}
	public StringBuffer list(LanguagePair list) {
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
		todo.todo(id);
		buffer.append('>');
		return buffer;
	}
	public StringBuffer rule(String id, TaggedData<?> rhs) {
		todo.done(id);
		this.id(id);
		buffer.append(" ::= ");
		g.visit(this, rhs);
		buffer.append("\n");
		return buffer;
	}
	@Override
	public StringBuffer top(Grammar g, WorkList<String> rules) {
		todo = rules;
		this.g = g;
		for (String id : todo) {
			g.visit(this, id);
		}
		return buffer;
	}
}
