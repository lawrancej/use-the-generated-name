package languageV2;

import java.util.HashSet;
import java.util.Set;

public class Nullable extends AbstractVisitor<Boolean> {
	boolean flag = true;
	String root;
	Set<String> nulls = new HashSet<String>();
	public Nullable(Grammar g) {
		super(g, new WorkList<String>());
	}
	public Boolean symbol(Character c) {
		return false;
	}
	public Boolean list(LanguagePair list) {
		if (list == null) return true;
		boolean result = g.visit(this, list.left) && g.visit(this, list.right);
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
		return nulls.contains(id);
	}
	public Boolean rule(String id, TaggedData<?> rhs) {
		boolean result = g.visit(this, rhs);
		if (result) {
			nulls.add(id);
		}
		return result;
	}
	public Boolean bottom() {
		return false;
	}
	public Boolean reduce(Boolean accumulator, String identifier, Boolean current) {
		// Return only the first result.
		if (flag) {
			flag = false;
			root = identifier;
		}
		return nulls.contains(root);
	}
}
