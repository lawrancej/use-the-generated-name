package languageV2;

/**
 * Is the identifier a nonterminal?
 */
public class Nonterminal implements Visitor<Boolean> {
	Grammar g;
	WorkList<String> todo;
	String label;
	protected Nonterminal(String label) {
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
		todo.todo(id);
		return todo.visited(label);
	}

	public Boolean rule(String id, TaggedData<?> rhs) {
		todo.done(id);
		return g.visit(this, rhs);
	}

	public Boolean top(Grammar g, WorkList<String> rules) {
		todo = rules;
		this.g = g;
		for (String id : todo) {
			if (g.visit(this, id)) {
				return true;
			}
		}
		return false;
	}
}
