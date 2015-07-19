package languageV2.traversal;

import languageV2.Language;
import languageV2.Node;

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
	 * Visit symbol: <code>c</code>
	 */
	T symbol(Node<Character,Character> language);
	/**
	 * Visit a list of languages: <code>abc...</code>
	 */
	T list(Node<Node<?,?>,Node<?,?>> language);
	/**
	 * Visit a set of languages <code>a|b|c|...</code>
	 */
	T set(Node<Node<?,?>,Node<?,?>> language);
	/**
	 * Get the worklist of visited identifiers.
	 * @return the work list.
	 */
	WorkQueue<Node<String,Void>> getWorkList();
	/**
	 * Visit an identifier (on the right hand side).
	 * @param id The identifier
	 */
	T id(Node<String,Void> id);
	/**
	 * Visit a rule of the form `id -> rhs`
	 * @param id The identifier
	 * @param rhs The identifier's right hand side
	 */
	T rule(Node<Node<String,Void>,Node<?,?>> rule);
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
