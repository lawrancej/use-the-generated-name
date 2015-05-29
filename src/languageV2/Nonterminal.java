package languageV2;

/**
 * Is the identifier a nonterminal?
 */
public class Nonterminal extends AbstractVisitor<Boolean> {
	String label;
	protected Nonterminal(Grammar g, WorkList<String> todo, String label) {
		super(g, todo);
		this.label = label;
	}
	public Boolean symbol(Character c) {
		return false;
	}
	public Boolean list(LanguagePair list) {
		if (list == null) return false;
		return g.visit(this, list.left) || g.visit(this, list.right);
	}
	public Boolean loop(TaggedData<?> language) {
		return g.visit(this, language);
	}
	public Boolean set(SetOfLanguages set) {
		boolean result = false;
		if (set != null) {
			for (TaggedData<?> l : set) {
				result = result || g.visit(this, l);
			}
		}
		return result;
	}
	public Boolean id(String id) {
		return todo.visited(label);
	}
	public Boolean rule(String id, TaggedData<?> rhs) {
		return g.visit(this, rhs);
	}
	public Boolean bottom() {
		return false;
	}
	public Boolean reduce(Boolean a, Boolean b) {
		return a || b;
	}
}
