package languageV2;

// Compute the first set
public class FirstSet implements Visitor<TaggedData<?>> {
	private Grammar g;
	WorkList<String> todo;
	protected FirstSet() {}
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
	@Override
	public TaggedData<?> id(String id) {
		todo.todo(id);
		return Grammar.reject;
	}
	@Override
	public TaggedData<?> rule(String id, TaggedData<?> rhs) {
		todo.done(id);
		return g.visit(this, rhs);
	}
	@Override
	public TaggedData<?> top(Grammar g, WorkList<String> rules) {
		TaggedData<?> result = Grammar.reject;
		todo = rules;
		this.g = g;
		for (String id : todo) {
			result = g.or(result, g.visit(this, id));
		}
		return result;
	}
}
