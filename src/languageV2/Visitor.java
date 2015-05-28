package languageV2;

/**
 * Traverse a language specification.
 * @param <T> The return type of the visitor.
 */
public interface Visitor<T> {
	/**
	 * Visit symbol `c`
	 * @param c Character
	 */
	T symbol(Character c);
	/**
	 * Visit a list of languages `abc...`
	 * @param list The language list
	 */
	T list(LanguagePair list);
	/**
	 * Visit a language loop `a*`
	 * @param loop The language `a`
	 */
	T loop(TaggedData<?> language);
	/**
	 * Visit a set of languages `a|b|c|...`
	 * @param set A set of languages
	 */
	T set(SetOfLanguages set);
	// FIXME: pass visited as a boolean here?
	/**
	 * Visit an identifier
	 * @param id The identifier label
	 */
	T id(String id);
	// FIXME: outside of here, call todo.done(id)?
	/**
	 * Visit a rule of the form `id -> rhs`
	 * @param id The identifier label
	 * @param rhs The identifier definition
	 */
	T rule(String id, TaggedData<?> rhs);
	// FIXME: Hide the WorkList from the end user?
	// I don't think it's possible. :-(
	/**
	 * Visit rules in language lazily by need.
	 * @param g A language grammar
	 * @param rules A worklist of rules
	 */
	T top(Grammar g, WorkList<String> rules);
}
