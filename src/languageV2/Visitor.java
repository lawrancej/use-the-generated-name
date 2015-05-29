package languageV2;

/**
 * Traverse a language specification lazily by need.
 * @param <T> The return type of the visitor.
 */
public interface Visitor<T> {
	/**
	 * Grammar traversal requires a work list of identifiers to visit to enable termination.
	 * @return the work list.
	 */
	WorkList<String> getWorkList();
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
	 * The default result.
	 * 
	 * @return the default result
	 */
	T bottom();
	/**
	 * Accumulate results during worklist traversal.
	 * @param a
	 * @param b
	 * @return
	 */
	T reduce(T a, T b);
}
