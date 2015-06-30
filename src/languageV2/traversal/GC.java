package languageV2.traversal;

import util.TaggedData;
import util.TaggedDataPair;
import languageV2.Language;
import languageV2.SetOfLanguages;

public class GC extends AbstractVisitor<Void> {
	public GC(Language g) {
		super(g);
	}
	public Void symbol(int id, Character c) {
		return null;
	}
	public Void list(int id, TaggedDataPair list) {
		if (list == null) {
			return null;
		}
		g.visit(this, list.left);
		g.visit(this, list.right);
		return null;
	}
	public Void loop(int id, TaggedData<?> language) {
		g.visit(this, language);
		return null;
	}
	public Void set(int id, SetOfLanguages set) {
		if (set == null) {
			return null;
		}
		for (TaggedData<?> l : set) {
			g.visit(this, l);
		}
		return null;
	}
	public Void id(Language.Id id) {
		return null;
	}
	public Void rule(Language.Id id, TaggedData<?> rhs) {
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
