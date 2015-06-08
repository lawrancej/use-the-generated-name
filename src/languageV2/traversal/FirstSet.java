package languageV2.traversal;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.Node;
import util.TaggedData;

// Compute the first set
public class FirstSet extends AbstractVisitor<TaggedData<?>> {
	public FirstSet(Language g) {
		super(g, new WorkList<String>());
	}
	public TaggedData<?> symbol(Character c) {
		return g.symbol(c);
	}
	public TaggedData<?> loop(TaggedData<?> loop) {
		return g.visit(this, loop);
	}
	public TaggedData<?> list(Node<TaggedData<?>> list) {
		if (list == null) return bottom();
		TaggedData<?> result = g.visit(this, list.data);
		if (g.nullable(list.data)) {
			result = g.or(result, list(list.next));
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
