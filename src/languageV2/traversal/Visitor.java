package languageV2.traversal;

import languageV2.Language;
import util.Node;
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
	 * @param id TODO
	 * @param c Character
	 */
	T symbol(Node<Character> language);
	/**
	 * Visit a list of languages `abc...`
	 * @param list The language list
	 */
	T list(Node<TaggedDataPair> list);
	/**
	 * Visit a language loop `a*`
	 * @param loop The language `a*`
	 */
	T loop(Node<Node<?>> language);
	/**
	 * Visit a set of languages `a|b|c|...`
	 * @param set A set of languages
	 */
	T set(Node<TaggedDataPair> set);
	/**
	 * Get the worklist of visited identifiers.
	 * @return the work list.
	 */
	WorkQueue<Language.Id> getWorkList();
	/**
	 * Visit an identifier (on the right hand side).
	 * @param id The identifier
	 */
	T id(Language.Id id);
	/**
	 * Visit a rule of the form `id -> rhs`
	 * @param id The identifier
	 * @param rhs The identifier's right hand side
	 */
	T rule(Language.Id id, Node<?> rhs);
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
	/**
	 * Pre-traversal method.
	 */
	void begin();
	/**
	 * Post-traversal method.
	 */
	void end();
}
