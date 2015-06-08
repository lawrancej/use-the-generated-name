package languageV2.traversal;

import util.Node;
import util.TaggedData;
import languageV2.Language;
import languageV2.SetOfLanguages;

public class GC extends AbstractVisitor<Void> {
	public GC(Language g) {
		super(g, new WorkList<String>());
	}
	public Void symbol(Character c) {
		return null;
	}
	public Void list(Node<TaggedData<?>> list) {
		if (list == null) {
			return null;
		}
		g.visit(this, list.data);
		list(list.next);
		return null;
	}
	public Void loop(TaggedData<?> language) {
		g.visit(this, language);
		return null;
	}
	public Void set(SetOfLanguages set) {
		if (set == null) {
			return null;
		}
		for (TaggedData<?> l : set) {
			g.visit(this, l);
		}
		return null;
	}
	public Void id(String id) {
		return null;
	}
	public Void rule(String id, TaggedData<?> rhs) {
		g.visit(this, rhs);
		return null;
	}
	public Void bottom() {
		return null;
	}
	public boolean done(Void accumulator) {
		return false;
	}
	public Void reduce(Void accumulator, Void current) {
		return null;
	}
}
