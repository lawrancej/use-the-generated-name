package languageV2.traversal;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.TaggedData;
import util.TaggedDataPair;

// Compute the first set
public class FirstSet extends AbstractVisitor<TaggedData<?>> {
	public FirstSet(Language g) {
		super(g);
	}
	public TaggedData<?> symbol(int id, Character c) {
		return g.symbol(c);
	}
	public TaggedData<?> loop(TaggedData<TaggedData<?>> loop) {
		return g.visit(this, loop.data);
	}
	public TaggedData<?> list(TaggedData<TaggedDataPair> language) {
		TaggedDataPair pair = language.data;
		if (pair == null) return bottom();
		TaggedData<?> result = g.visit(this, pair.left);
		if (g.nullable(pair.left)) {
			result = g.or(result, g.visit(this, pair.right));
		}
		return result;
	}
	public TaggedData<?> set(int id, SetOfLanguages set) {
		TaggedData<?> result = bottom();
		if (set == null) return result;
		for (TaggedData<?> l : set) {
			result = g.or(result, g.visit(this, l));
		}
		return result;
	}
	public TaggedData<?> id(Language.Id id) {
		return bottom();
	}
	public TaggedData<?> rule(Language.Id id, TaggedData<?> rhs) {
		return g.visit(this, rhs);
	}
	public TaggedData<?> bottom() {
		return Language.reject;
	}
	public TaggedData<?> reduce(TaggedData<?> accumulator, TaggedData<?> current) {
		return g.or(accumulator, current);
	}
	@Override
	public boolean done(TaggedData<?> accumulator) {
		return false;
	}
}
