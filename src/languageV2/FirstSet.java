package languageV2;

import java.util.HashSet;

import languageV2.Grammar.Construct;
import languageV2.Grammar.Id;

// Compute the first set
public abstract class FirstSet implements Visitor<TaggedData<?>> {
	/*
	private Grammar g;
	public TaggedData<?> first(TaggedData<?> language) {
		return first(new HashSet<Id>(), language);
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
	@Override
	public TaggedData<?> id(String id, TaggedData<?> rhs) {
		if (!visited.contains(id)) {
			visited.add(id);
			return g.visit(this, rhs);
		}
		return Grammar.reject;
	}
	@Override
	public TaggedData<?> result() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void rule(String id, TaggedData<?> rhs) {
		// TODO Auto-generated method stub
		
	}
	*/
}
