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
		if (todo.visited(id)) {
			if (ids.contains(id)) {
				return g.id(dc);
			}
		} else {
			TaggedData<?> result = g.visit(this, id);
			if (result != bottom()) {
				return g.id(dc);
			}
		}
		return bottom();
//		return g.id(dc);
	}
	public TaggedData<?> rule(String id, TaggedData<?> rhs) {
		String dc = "D" + c + id;
		TaggedData<?> derivation = g.visit(this,  rhs);
		System.out.print(id);
		System.out.println(" will be ");
		System.out.println(g.toString(derivation));
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
