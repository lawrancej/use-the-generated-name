package languageV2.traversal;

import java.util.HashMap;
import java.util.Map;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.TaggedData;
import util.TaggedDataPair;

public class Derivative extends AbstractVisitor<TaggedData<?>> {
	public Character c;
	Map<Language.Id, Language.Id> ids = new HashMap<Language.Id, Language.Id>();
	public Derivative(Language g) {
		super(g);
	}
	public TaggedData<?> symbol(TaggedData<Character> language) {
		Character c = language.data;
		// Dc(c|.) = e
		if (c == null || this.c == c) {
			return Language.empty;
		}
		// Dc(c') = 0
		return bottom();
	}
	public TaggedData<?> list(TaggedData<TaggedDataPair> language) {
		TaggedDataPair list = language.data;
		// Dc(e) = 0
		if (list == null) return bottom();
		// Dc(ab) = Dc(a)b + nullable(a)Dc(b)
		TaggedData<?> result = g.list(g.accept(this, list.left), list.right);
		if (g.nullable(list.left)) {
			return g.or(result, g.accept(this, list.right));
		}
		return result;
	}
	public TaggedData<?> loop(TaggedData<TaggedData<?>> loop) {
		TaggedData<?> language = loop.data;
//		return g.visit(this, g.list(language, loop));
		return g.list(g.accept(this, language), loop);
	}
	public TaggedData<?> set(TaggedData<SetOfLanguages> language) {
		SetOfLanguages set = language.data;
		TaggedData<?> result = bottom();
		// Dc(0) = 0
		if (set == null) return result;
		// Dc(a+b) = Dc(a) + Dc(b)
		for (TaggedData<?> l : set) {
			result = g.or(result, g.accept(this, l));
		}
		return result;
	}
	private Language.Id getReplacement(Language.Id id) {
		if (!ids.containsKey(id)) {
			Language.Id replacement = g.id();
			ids.put(id, replacement);
			return replacement;
		}
		else {
			return ids.get(id);
		}
	}
	public TaggedData<?> id(Language.Id id) {
		// Handle left-recursion: return DcId if we're visiting Id -> Id
		if (todo.visiting(id)) {
			return getReplacement(id);
		}
		// Visit rule Id -> rhs, if we haven't already visited it.
		if (!todo.visited(id)) {
			return g.acceptRule(this, id);
		}
		// By this point, we've seen the identifier on the rhs before.
		// If the identifier derives a non-empty set, return the identifier
		if (ids.containsKey(id)) {
			return ids.get(id);
		}
		// Otherwise, return the empty set
		return bottom();
	}
	public TaggedData<?> rule(Language.Id id, TaggedData<?> rhs) {
		// Visit the rhs
		TaggedData<?> derivation = g.accept(this,  rhs);
		
		// Don't create a rule that rejects
		if (derivation == bottom()) {
			return derivation;
		}
		
		// Make a note of identifiers that derive non-empty sets
		TaggedData<?> result = g.derives(getReplacement(id), derivation);
		return result;
	}
	public TaggedData<?> bottom() {
		return Language.reject;
	}
	public TaggedData<?> reduce(TaggedData<?> accumulator, TaggedData<?> current) {
		if (accumulator == bottom()) return current;
		else return accumulator;
	}
	public boolean done(TaggedData<?> accumulator) {
		return false;
	}
	public void begin() {
		ids.clear();
	}
}
