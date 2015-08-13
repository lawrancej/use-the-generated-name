package com.dictorobitary;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Specify a context-free language to parse or generate strings.
 * 
 * <p>
 * Internally, a graph data structure represents the grammar.
 * To conserve memory and improve performance,
 * without sacrificing the ability to parse ambiguous grammars,
 * this class performs compaction by construction,
 * caches graph nodes to ensure no duplicates,
 * 
 * Nodes are structured as follows:
 * 
 * <table border="1">
 * <tr>
 * <td>Tag</td>
 * <td>Left</td>
 * <td>Right</td>
 * </tr>
 * <tr>
 * <td>8 bits</td>
 * <td>16 bits</td>
 * <td>16 bits</td>
 * </tr>
 * </table>
 * <p>
 * The tag represents the node's type.
 * <ul>
 * <li>0 is a character class: the left and right are the Unicode code points in the range.
 * If the left and right are identical, it's single character.
 * Any character (regex dot) is a special preallocated character class 
 * <li>1 is a sequence (list), left followed by right.
 * The left and right are internal pointers to other graph nodes.
 * If the right points back to the list itself, it's a loop.
 * The empty sequence (epsilon) is a special preallocated list 
 * <li>2 is a set (choice), left or right.
 * The left and right are internal pointers to other graph nodes.
 * The empty set (reject) is a special preallocated list 
 * <li>3 is an identifier. As such, it enables recursion,
 * but it is overloaded to handle practical concerns such as tokenization,
 * reduction actions, result sets, and so forth. 
 * Two identifiers are preallocated: the result set root, and the language root.
 * The left is a small bit field that stores whether the identifier is nullable, skippable, modifiable,
 * a terminal, a token, a result root (eps*)
 * The right is an internal pointer to another graph 
 * </ul>
 * 
 * Actions (reductions) are attached to the grammar's identifiers externally to allow re-use of a grammar
 * but with different reduction actions.
 * 
 * Labels are attached to the grammar's identifiers externally to allow printing a grammar in a friendly manner.
 * 
 * @author Joseph Lawrance
 *
 */
public class Language {
	/* Node types */
	public static final byte SYMBOL = 0;
	public static final byte LIST = 1;
	public static final byte SET = 2;
	public static final byte ID = 3;
	public static final byte RULE = 4;
	public static final byte LOOP = 5;
	public static final byte TOKEN = 6;
	public static final byte ACTION = 7;
	public static final byte RESULT = 8;
	public static final byte SKIP = 9;

	// Graph nodes consist of a tag, a left and a right side
	private byte[] tag;
	private int[] left;
	private int[] right;

	/* Track allocations to graph nodes */
	private int pointer = 0;
	
	/** Match the empty list (empty sequence). */
	public final int empty;
	/** Match any character, equivalent to regular expression dot. */
	public final int any;
	/** Reject everything (the empty set). */
	public final int reject;
	/** The language definition. The root of all traversal. */
	private int definition;
	
	public final String name;
	/** Construct a language specification */
	public Language(String name, int capacity) {
		this.name = name;
		
		// Pre-allocate graph nodes in arrays.
		tag = new byte[capacity];
		left = new int[capacity];
		right = new int[capacity];
		
		// Create singletons (by default, reject)
		definition = reject = create(SET, 0, 0);
		empty = create(LIST, 0, 0);
		any = create(SYMBOL, 0, 0);
	}
	
	public Language(String name) {
		this(name, 1024*1024);
	}
	
	private int getCached(Map<Long, Integer> cache, long key) {
		return cache.get(key);
	}
	private <K> int createCached(Map<K, Integer> cache, K key, byte type, int left, int right) {
		if (!cache.containsKey(key)) {
			int result = create(type, left, right);
			cache.put(key, result);
			return result;
		}
		return cache.get(key);
	}
	// "Construct" a graph node
	private int create(byte tag, int left, int right) {
		++pointer;
		this.tag[pointer] = tag;
		this.left[pointer] = left;
		this.right[pointer] = right;
		return pointer;
	}

	public byte tag(int language) {
		return this.tag[language];
	}
	public int left(int language) {
		return this.left[language];
	}
	public int right(int language) {
		return this.right[language];
	}
	public int id(int language) {
		return language;
	}
	/**
	 * Accept a visitor.
	 * 
	 * @param visitor
	 * @param language
	 * @return
	 */
	public <T> T accept(Visitor<T> visitor, int language) {
		switch(tag(language)) {
		case ID:
			visitor.getWorkList().todo(language);
			return visitor.id(language);
		case LIST:
			if (language == empty) {
				return visitor.empty(language);
			}
			return visitor.list(language);
		case SET:
			if (language == reject) {
				return visitor.reject(language);
			}
			return visitor.set(language);
		case SYMBOL:
			if (language == any) {
				return visitor.any(language);
			}
			return visitor.symbol(language);
		case LOOP:
			return visitor.loop(language);
		default:
			return null;
		}
	}


	private Map<Long, Integer> rangeCache = new HashMap<Long, Integer>();
	/**
	 * Match a character.
	 * 
	 * @param character to match (e.g., <code>c</code>)
	 * @return A language matching the character: <code>c</code>
	 */
	public int symbol(char character) {
		return range(character,character);
	}
	/**
	 * Match a range of characters (i.e., character class).
	 * @param from character
	 * @param to character
	 * @return A language matching <code>[from-to]</code>
	 */
	public int range(char from, char to) {
		// Swap order if from is greater than to
		char tmp = from;
		if (from > to) {
			from = to;
			to = tmp;
		}
		long key = (from << 16) ^ to;
		return createCached(rangeCache, key, SYMBOL, from,to);
	}
	
	private Map<Long, Integer> listCache = new HashMap<Long, Integer>();
	private boolean skipDefinedIdentifiers = false;
	// Skip through defined identifiers
	private int getRHS(int language) {
		if (skipDefinedIdentifiers ) {
			int key = id(language);
			if (tag(language) == ID && rules.containsKey(key)) {
				int result = rules.get(key);
				return right(result);
			}
		}
		return language;
	}
	// See: http://cs.brown.edu/people/jes/book/pdfs/ModelsOfComputation.pdf
	private int listInstance(int left, int right) {
		left = getRHS(left);
		right = getRHS(right);
		// r0 = 0r = 0
		if (left == reject || right == reject) {
			return reject;
		}
		// re = er = r
		if (left == empty) { return right; }
		if (right == empty) { return left; }
		// We shift left to guarantee ab != ba, and to guarantee aa != bb
		long key = ((long)left << 32) ^ right;
		return createCached(listCache, key, LIST, left, right);
	}
	private int list(int[] nodes, int i) {
		if (i >= nodes.length) {
			return empty;
		} else {
			return listInstance(nodes[i], list(nodes, i+1));
			// return listInstance(separator, listInstance(nodes[i], list(nodes, i+1, separator)));
		}
	}
	/**
	 * Match a sequence of languages.
	 * 
	 * @param sequence of languages (a,b,c,...)
	 * @return A language matching the sequence in order: <code>abcd...</code>
	 */
	public int list(int... sequence) {
		return list(sequence, 0);
	}
	
	// Convert a string into an array of symbols
	private int[] explode(String string) {
		int[] array = new int[string.length()];
		for (int i = 0; i < string.length(); i++) {
			array[i] = symbol(string.charAt(i));
		}
		return array;
	}
	/**
	 * Match a string literally.
	 * 
	 * @param string to match literally (e.g., <code>hello</code>)
	 * @return A language matching the string: <code>hello<code>
	 */
	public int string(String string) {
		return list(explode(string), 0);
	}
	private Map<Long, Integer> setCache = new HashMap<Long, Integer>();

	private int orInstance(int left, int right) {
		// Skip through defined identifiers
		left = getRHS(left);
		right = getRHS(right);
		// r+0 = 0+r = r
		if (left == reject) { return right; }
		if (right == reject) { return left; }
		// r+r = r
		if (left == right) { return left; }
		// r+(s+r) = (r+s)+r = r+(r+s) = (s+r)+r = r+s
		if (tag(left) == SET) {
			if (left(left) == right || right(left) == right) return left;
		}
		if (tag(right) == SET) {
			if (left(right) == left || right(right) == left) return right;
		}
		// Ensure a canonical order for sets
		int tmp = left;
		if (left > right) {
			left = right;
			right = tmp;
		}
		long key = ((long)left << 32) ^ right;
		return createCached(setCache, key, SET, left, right);
	}
	private int or(int[] nodes, int i) {
		if (i >= nodes.length) {
			return reject;
		} else {
			return orInstance(nodes[i], or(nodes, i+1));
		}
	}
	/**
	 * Match any of the options.
	 * 
	 * @param options to match (a,b,c,...,z)
	 * @return A language matching one of the options <code>(a|b|c|...|z)</code>
	 */
	public int or(int... options) {
		return or(options, 0);
	}
	/**
	 * Match one of the characters in a string.
	 * @param string of characters
	 * @return A language matching one of the characters in the string.
	 */
	public int oneOf(String string) {
		return or(explode(string), 0);
	}
	/**
	 * Match a sequence, optionally.
	 * 
	 * @param sequence to match optionally (a,b,c,...,z)
	 * @return A language matching the sequence optionally <code>(abc...z)?</code>
	 */
	public int option(int... sequence) {
		return or(empty, list(sequence));
	}
	
	private Map<Integer, Integer> loopCache = new HashMap<Integer, Integer>();
	/**
	 * Match a sequence zero or more times.
	 * 
	 * @param sequence to match repeatedly (e.g., <code>ab</code>)
	 * @return A language matching the sequence zero or more times: <code>(ab)*</code>
	 */
	public int many(int... sequence) {
		int language = list(sequence);
		// 0* = e* = e
		if (language == empty || language == reject) { return empty; }
		if (tag(language) == LOOP) return language;
		return createCached(loopCache, language, LOOP, language, any);
	}
	/**
	 * Matches zero or more occurrences of language, separated by separator.
	 * 
	 * @param language to match
	 * @param separator to use
	 * @return
	 */
	public int sepBy(int language, int separator) {
		return 0;
	}
	/**
	 * Matches one or more occurrences of language, separated by separator.
	 * @return
	 */
	public int sepBy1(int language, int separator) {
		return 0;
	}
	/**
	 * Match a sequence one or more times.
	 * @param sequence to match repeatedly (e.g., <code>ab</code>)
	 * @return A language matching the sequence one or more times: <code>(ab)+</code>
	 */
	public int many1(int... sequence) {
		return list(list(sequence), many(sequence));
	}
	// Identifier lookup by name
	private Map<String, Integer> labels = new HashMap<String, Integer>();
	private Set<Integer> ids = new LinkedHashSet<Integer>();
	/**
	 * Create or use an identifier (a terminal or nonterminal variable).
	 * 
	 * @param label for the identifier.
	 * @return The identifier.
	 */
	public int id(String label) {
		assert label != null;
		assert labels != null;
		if (labels.containsKey(label)) {
			return labels.get(label);
		}
		int result = id();
		labels.put(label, result);
		return result;
	}
	/**
	 * Create an identifier (a terminal or nonterminal variable).
	 * @return The identifier
	 */
	public int id() {
		int result = create(ID, 0, 0);
		ids.add(result);
		return result;
	}
	/**
	 * Get the language definition
	 * @return the root/start node in the language data structure.
	 */
	public int definition() {
		return definition;
	}
	/** Tokenization separator */
	
	Map<Integer, Integer> rules = new HashMap<Integer, Integer>();
	
	Map<Integer, Integer> reverse = new HashMap<Integer, Integer>();
	private int undefine(int id) {
		ids.remove(id);
		rules.remove(id(id));
		return reject;
	}

	/**
	 * Create a rule (production).
	 * 
	 * <p>
	 * <code>label -> rhs</code>
	 * <p>
	 * Side effect: the first call to <code>rule</code> defines the starting identifier.
	 * Subsequent calls to <code>rule</code> add rules for the identifier.
	 * 
	 * @param label for the identifier
	 * @param rhs the right hand side
	 * @return The identifier, or reject (if the rhs rejects or <code>id -> id</code>).
	 */
	public int rule(String label, int... rhs) {
		assert label != null;
		assert rhs != null;
		int result = rule(id(label), rhs);
		if (result == reject) {
//			labels.remove(label);
		}
		return result;
	}
	/**
	 * Create a rule (production).
	 * 
	 * <p>
	 * <code>id -> rhs</code>
	 * <p>
	 * Side effect: the first call to <code>rule</code> defines the starting identifier.
	 * Subsequent calls to <code>rule</code> add rules for the identifier.
	 * 
	 * @param id the identifier
	 * @param rhs the right hand side
	 * @return The identifier, or reject (if the rhs rejects or <code>id -> id</code>).
	 */
	public int rule(int id, int... rhs) {
		assert rhs != null;
		int right = list(rhs);
		right = getRHS(right);
		// If the right rejects, or Id -> Id literally, remove the identifier and reject
		if (right == reject || id == right) {
//			return undefine(id);
			return reject;
		}
		// If the right hand side is just an undefined identifier, don't create a rule, just return the existing identifier
		int key = id(id);
		if (tag(right) == ID && !rules.containsKey(key)) {
//			undefine(id);
			return right;
		}
		// If we defined this language already with a different identifier, return the existing identifier
		if (reverse.containsKey(id(right))) {
			int storedId = reverse.get(id(right));
			if (rules.containsKey(id(storedId)) && !rules.containsKey(key)) {
				return storedId;
			}
		}
		reverse.put(id(right), id);
		int node = createCached(rules, key, RULE, id, right);
		assert left(node) == id;
		// If the language is undefined, make this the starting identifier
		if (definition == reject) {
			definition = id;
		}
		return id;
	}
	
	/**
	 * Surround a sequence
	 * @param language
	 * @return
	 */
	public int token(int id, int... sequence) {
		return rule(id, list(sequence));
	}
	
	/**
	 * Define the separator for tokenization.
	 * 
	 * @param separator for tokenization
	 * @return the language
	public int separator(int... separator) {
		return this.separator = list(separator);
	}
	 */

	/**
	 * Specify a language.
	 * <p>
	 * For regular expressions, surround the definition with <code>define()</code>.
	 * For grammars, the first derivation is the starting nonterminal.
	 * 
	 * @param language The language
	 */
	public void define(int... language) {
		definition = list(language);
	}
	/**
	 * Accept visitor into a rule of the form: <code>id -> right</code>
	 * 
	 * @param visitor
	 * @param id
	 * @return
	 */
	public <T> T acceptRule(Visitor<T> visitor, int id) {
		visitor.getWorkList().done(id);
		// Defined identifiers
		if (rules.containsKey(id(id))) {
			return visitor.rule(rules.get(id));
		}
		// Undefined identifiers
		return visitor.reject(reject);
	}
	public final Compute get = new Compute(this);
	public String toString() {
		return get.printer.compute().toString();
	}
}
