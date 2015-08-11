package com.dictorobitary;

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
	 * Visit any symbol: <code>.</code>
	 */
	T any(int language);
	/**
	 * Visit symbol range: <code>[from-to]</code>
	 */
	T symbol(int language);
	/**
	 * Visit empty list: <code>&epsilon;</code>
	 */
	T empty(int language);
	/**
	 * Visit a list of languages: <code>abc...</code>
	 */
	T list(int language);
	/**
	 * Visit a loop: <code>a*</code>
	 * @param language
	 */
	T loop(int language);
	/**
	 * Visit empty set: <code>&#8709;</code>
	 */
	T reject(int language);
	/**
	 * Visit a set of languages <code>a|b|c|...</code>
	 */
	T set(int language);
	/**
	 * Get the worklist of visited identifiers.
	 * @return the work list.
	 */
	WorkQueue<Integer> getWorkList();
	/**
	 * Visit an identifier (on the right hand side).
	 * @param id The identifier
	 */
	T id(int id);
	/**
	 * Visit a rule of the form `id -> rhs`
	 * @param id The identifier
	 * @param rhs The identifier's right hand side
	 */
	T rule(int rule);
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
	public T reduce(T accumulator, T current);
	/**
	 * Pre-traversal method.
	 */
	void begin();
	/**
	 * Post-traversal method.
	 */
	T end(T accumulator);
}
