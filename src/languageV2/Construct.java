package languageV2;

/**
 * A language is a set of strings (lists of symbols).
 * 
 * Specifying a finite language requires symbols, lists, and sets.
 * Specifying an infinite language also requires repetition or recursion.
 * Loops specify repetition, and identifiers enable recursion.
 */
public enum Construct {
	// Finite language specifiers
	SYMBOL,	LIST, SET,
	// Infinite language specifiers
	LOOP, ID,
}