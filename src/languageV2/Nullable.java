package languageV2;

public class Nullable extends AbstractVisitor<Boolean> {
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
		return todo.visited(id);
	}
	public Boolean rule(String id, TaggedData<?> rhs) {
		return g.visit(this, rhs);
	}
	public Boolean bottom() {
		return false;
	}
	public Boolean reduce(Boolean a, Boolean b) {
		return a || b;
//		if (a == bottom()) return b;
//		else return a;
	}
}
