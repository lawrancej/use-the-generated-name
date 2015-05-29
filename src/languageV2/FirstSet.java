package languageV2;

// Compute the first set
public class FirstSet extends AbstractVisitor<TaggedData<?>> {
	protected FirstSet(Grammar g) {
		super(g, new WorkList<String>());
	}
	public TaggedData<?> symbol(Character c) {
		return g.symbol(c);
	}
	public TaggedData<?> loop(TaggedData<?> loop) {
		return g.visit(this, loop);
	}
	public TaggedData<?> list(LanguagePair pair) {
		if (pair == null) return bottom();
		TaggedData<?> result = g.visit(this, pair.left);
		if (g.nullable(pair.left)) {
			result = g.or(result, g.visit(this, pair.right));
		}
		return result;
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
		return bottom();
	}
	public TaggedData<?> rule(String id, TaggedData<?> rhs) {
		return g.visit(this, rhs);
	}
	public TaggedData<?> bottom() {
		return Grammar.reject;
	}
	public TaggedData<?> reduce(TaggedData<?> a, TaggedData<?> b) {
		return g.or(a, b);
	}
}
