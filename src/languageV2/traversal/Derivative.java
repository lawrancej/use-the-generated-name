package languageV2.traversal;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.TaggedData;
import util.TaggedDataPair;

public class Derivative extends AbstractVisitor<TaggedData<?>> {
	public Character c;
	public Derivative(Language g) {
		super(g, new WorkList<String>());
	}
	public TaggedData<?> symbol(Character c) {
		if (c == null || this.c == c) {
			return Language.empty;
		}
		return bottom();
	}
	public TaggedData<?> list(TaggedDataPair list) {
		if (list == null) return bottom();
		TaggedData<?> result = g.list(g.visit(this, list.left), list.right);
		if (g.nullable(list.left)) {
			return g.or(result, g.visit(this, list.right));
		}
		return result;
	}
	public TaggedData<?> loop(TaggedData<?> language) {
		return g.visit(this, g.list(language, g.many(language)));
	}
	public TaggedData<?> set(SetOfLanguages set) {
		TaggedData<?> result = bottom();
		if (set == null) return result;
		for (TaggedData<?> l : set) {
			result = g.or(result, g.visit(this, l));
		}
		return result;
	}
	public TaggedData<?> id(String id) {
		String dc = "D" + c + id;
		return g.id(dc);
	}
	public TaggedData<?> rule(String id, TaggedData<?> rhs) {
		String dc = "D" + c + id;
		TaggedData<?> result = g.derives(dc, g.visit(this, rhs));
		return result;
	}
	public TaggedData<?> bottom() {
		return Language.reject;
	}
	public TaggedData<?> reduce(TaggedData<?> accumulator, TaggedData<?> current) {
		if (accumulator == bottom()) return current;
		else return accumulator;
	}
	@Override
	public boolean done(TaggedData<?> accumulator) {
		return false;
	}
}
