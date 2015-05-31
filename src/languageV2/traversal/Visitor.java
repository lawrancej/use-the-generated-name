package languageV2.traversal;

import languageV2.SetOfLanguages;
import util.TaggedData;
import util.TaggedDataPair;

/**
 * Traverse a language specification.
 * 
 * Regular languages consist of symbols, lists, sets and loops,
 * whereas context-free languages also include identifiers and derivation rules.
 * Regular languages imply tree traversal; grammars imply graph traversal.
 * 
 * A work queue enables graph traversal termination.
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
	T list(TaggedDataPair list);
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
	 * Visit an identifier (on the right hand side).
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
	 * Accumulate results during work queue traversal.
	 * @param accumulator
	 * @param the current result
	 * @return the result
	 */
	T reduce(T accumulator, T current);
}
