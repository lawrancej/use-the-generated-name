package languageV2.traversal;

import java.util.HashSet;
import java.util.Set;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.TaggedData;
import util.TaggedDataPair;

public class Derivative extends AbstractVisitor<TaggedData<?>> {
	public Character c;
	Set<String> ids = new HashSet<String>();
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
//		return g.list(g.visit(this, language), g.many(language));
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
		// If we're looking it already (i.e., id -> id), return the identifier
		if (todo.visiting(id)) {
			return g.id(dc);
		}
		// If we haven't seen this yet, examine the rule for the identifier
		if (!todo.visited(id)) {
			g.visit(this, id);
		}
		// If the rule doesn't derive empty set, return the identifier
		if (ids.contains(id)) {
			return g.id(dc);
		}
		return bottom();
	}
	public TaggedData<?> rule(String id, TaggedData<?> rhs) {
		String dc = "D" + c + id;
		TaggedData<?> derivation = g.visit(this,  rhs);
		if (derivation == bottom()) {
			return derivation;
		} else {
			ids.add(id);
		}
		TaggedData<?> result = g.derives(dc, derivation);
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
