package languageV2;

// Compute the first set
public class FirstSet extends AbstractVisitor<TaggedData<?>> {
	protected FirstSet(Grammar g, WorkList<String> todo) {
		super(g, todo);
	}
	public TaggedData<?> symbol(Character c) {
		return g.symbol(c);
	}
	public TaggedData<?> loop(TaggedData<?> loop) {
		return g.visit(this, loop);
	}
	public TaggedData<?> list(LanguagePair pair) {
		if (pair == null) return Grammar.reject;
		TaggedData<?> result = g.visit(this, pair.left);
		if (g.nullable(pair.left)) {
			result = g.or(result, g.visit(this, pair.right));
		}
		return result;
	}
	public TaggedData<?> set(SetOfLanguages set) {
		if (set == null) return Grammar.reject;
		TaggedData<?> result = Grammar.reject;
		for (TaggedData<?> l : set) {
			result = g.or(result, g.visit(this, l));
		}
		return result;
	}
	public TaggedData<?> id(String id) {
		return Grammar.reject;
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
