package languageV2.traversal;

import java.util.HashSet;
import java.util.Set;

import languageV2.Language;
import languageV2.SetOfLanguages;
import util.Node;
import util.TaggedData;
import util.TaggedDataPair;

public class Nullable extends AbstractVisitor<Boolean> {
	Set<String> nulls = new HashSet<String>();
	public Nullable(Language g) {
		super(g, new WorkList<String>());
	}
	public Boolean symbol(Character c) {
		return false;
	}
	public Boolean list(Node<TaggedData<?>> list) {
		if (list == null) return true;
		boolean result = g.visit(this, list.data) && list(list.next);
		return result;
	}
	public Boolean loop(TaggedData<?> language) {
		return true;
	}
	public Boolean set(SetOfLanguages set) {
		if (set == null) return false;
		for (TaggedData<?> l : set) {
			if (g.visit(this, l)) {
				return true;
			}
		}
		return false;
	}
	public Boolean id(String id) {
		if (todo.visited(id)) {
			return nulls.contains(id);
		} else {
			boolean result = g.visit(this, id);
			if (result) {
				nulls.add(id);
			}
			return result;
		}
	}
	public Boolean rule(String id, TaggedData<?> rhs) {
		return g.visit(this, rhs);
	}
	public Boolean bottom() {
		return false;
	}
	public Boolean reduce(Boolean accumulator, Boolean current) {
		return current;
	}
	// Return only the first result.
	public boolean done(Boolean accumulator) {
		return true;
	}
}
