package languageV2;

/**
 * Traverse a language definition.
 * @param <T>
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
	/**
	 * Visit an identifier
	 * @param id The identifier label
	 */
	T id(String id);
	/**
	 * Visit a rule of the form `id -> rhs`
	 * @param id The identifier label
	 * @param rhs The identifier definition
	 */
	T rule(String id, TaggedData<?> rhs);
	/**
	 * Visit rules in language by need.
	 * @param g A language grammar
	 * @param rules A worklist of rules
	 */
	T top(Grammar g, WorkList<String> rules);
}
