package languageV2;

public class Nullable extends AbstractVisitor<Boolean> {
	boolean flag = true;
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
		if (todo.visited(id)) {
			return false;
		} else {
			return g.visit(this, g.rhs(id));
		}
	}
	public Boolean rule(String id, TaggedData<?> rhs) {
		return g.visit(this, rhs);
	}
	public Boolean bottom() {
		return false;
	}
	public Boolean reduce(Boolean accumulator, String identifier, Boolean current) {
		// The work queue needs to visit identifiers and 
		// Return only the first result.
		if (flag) {
			flag = false;
			return current;
		}
		return accumulator;
	}
	@Override
	public boolean done(Boolean accumulator) {
		return accumulator;
	}
}
