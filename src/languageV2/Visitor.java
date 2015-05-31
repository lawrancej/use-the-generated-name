package languageV2;

/**
 * Traverse a language specification.
 * 
 * Whereas regular languages imply tree traversal among symbols, lists, sets and loops,
 * context-free languages require graph traversal.
 * To enable graph traversal termination, the interface requires:
 * 
 * * a worklist of visited identifiers
 * * a means to visit identifiers and rules
 * * a means to accumulate the results of visiting several identifiers
 * 
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
	/**
	 * Get the worklist of visited identifiers.
	 * @return the work list.
	 */
	WorkQueue<String> getWorkList();
	/**
	 * Visit an identifier.
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
	 * Should we stop traversal early?
	 * @param accumulator
	 * @return whether traversal should end early
	 */
	boolean done(T accumulator);
	/**
	 * Accumulate results during worklist traversal.
	 * @param accumulator
	 * @param the current identifier
	 * @param the current result
	 * @return the result
	 */
	T reduce(T accumulator, String id, T current);
}
