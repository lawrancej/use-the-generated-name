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
 * pre-allocates graph nodes in an array,
 * uses fastutil type-specific maps and sets,
 * and encodes nodes into 64-bit numbers as follows:
 * 
 * <table border="1">
 * <tr>
 * <td>Tag</td>
 * <td>Left</td>
 * <td>Right</td>
 * </tr>
 * <tr>
 * <td>4 bits</td>
 * <td>30 bits</td>
 * <td>30 bits</td>
 * </tr>
 * </table>
 * <p>
 * The tag represents the node's type.
 * <ul>
 * <li>0 is a character class: the left and right are the Unicode code points in the range.
 * If the left and right are identical, it's single character.
 * Any character (regex dot) is a special preallocated character class node.
 * <li>1 is a sequence (list), left followed by right.
 * The left and right are internal pointers to other graph nodes.
 * If the right points back to the list itself, it's a loop.
 * The empty sequence (epsilon) is a special preallocated list node.
 * <li>2 is a set (choice), left or right.
 * The left and right are internal pointers to other graph nodes.
 * The empty set (reject) is a special preallocated list node.
 * <li>3 is an identifier. As such, it enables recursion,
 * but it is overloaded to handle practical concerns such as tokenization,
 * reduction actions, result sets, and so forth. 
 * Two identifiers are preallocated: the result set root, and the language root.
 * The left is a small bit field that stores whether the identifier is nullable, skippable, modifiable,
 * a terminal, a token, a result root (eps*)
 * The right is an internal pointer to another graph node.
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
	public final String name;
	public long[] pool;
	public int pointer;
	/** Construct a language specification */
	public Language(String name) {
		this.name = name;
		pool = new long[1024*1024*2];
		pool[0] = 0x000000; // any
		pool[1] = 0x100000; // empty list
		pool[2] = 0x200000; // reject
		pool[3] = 0x300000; // language root
		pool[4] = 0x300000; // result identifier
		pointer = 2;
	}
	public static enum Tag {
		SYMBOL,	LIST, SET, ID, RULE, LOOP,
		TOKEN, ACTION, RESULT, SKIP
	}
	public Tag tag(int language) {
		byte tag = (byte) ((pool[language] & 0xf00000) >> 60);
		switch (tag) {
		case 0x0: return Tag.SYMBOL;
		case 0x1: return Tag.LIST;
		case 0x2: return Tag.SET;
		case 0x3: return Tag.ID;
		case 0x4: return Tag.RULE;
		case 0x5: return Tag.LOOP;
		case 0x6: return Tag.TOKEN;
		case 0x7: return Tag.ACTION;
		case 0x8: return Tag.RESULT;
		case 0x9: return Tag.SKIP;
		default: return Tag.SKIP;
		}
	}
	public static int left(int language) {
		return (int) ((pool[language] & 0x0ffc00) >> 30);
	}
	public static int right(int language) {
		return (int) (pool[language] & 0x0003ff);
	}
	public static int id(int language) {
		return language;
	}
	public static int getCached(Map<Long, Integer> cache, long key) {
		return cache.get(key);
	}
	public static long createCached(Map<Long, Integer> cache, long key, Tag type, int left, int right) {
		if (!cache.containsKey(key)) {
			int result = Node.create(type, left, right);
			cache.put(key, result);
			return result;
		}
		return cache.get(key);
	}
	// Handy shortcut for the constructor call
	public static int create(Tag type, int left, int right) {
		long object = ((type.ordinal() << 60) | (left << 30) | (right));
		pool[++allocations] = object;
		return allocations;
	}
	/**
	 * Accept a visitor.
	 * 
	 * @param visitor
	 * @param language
	 * @return
	 */
	public static <T> T accept(Visitor<T> visitor, int language) {
		switch(Node.tag(language)) {
		case ID:
			visitor.getWorkList().todo(language);
			return visitor.id(language);
		case LIST:
			if (language == Language.empty) {
				return visitor.empty(language);
			}
			return visitor.list(language);
		case SET:
			if (language == Language.reject) {
				return visitor.reject(language);
			}
			return visitor.set(language);
		case SYMBOL:
			if (language == Language.any) {
				return visitor.any(language);
			}
			return visitor.symbol(language);
		case LOOP:
			return visitor.loop(language);
		default:
			return null;
		}
	}

	/** Match any character, equivalent to regular expression dot. */
	public static final Node<Character,Character> any = Node.create(Node.Tag.SYMBOL, null, null);
	/** Match the empty list (empty sequence). */
	public static final Node<Node<?,?>,Node<?,?>> empty = Node.create(Node.Tag.LIST, null, null);
	/** Reject everything (the empty set). */
	public static final Node<Node<?,?>,Node<?,?>> reject = Node.create(Node.Tag.SET, null, null);

	private Map<Integer, Node<Character,Character>> rangeCache = new HashMap<Integer, Node<Character,Character>>();
	/**
	 * Match a character.
	 * 
	 * @param character to match (e.g., <code>c</code>)
	 * @return A language matching the character: <code>c</code>
	 */
	public Node<Character,Character> symbol(char character) {
		return range(character,character);
	}
	/**
	 * Match a range of characters (i.e., character class).
	 * @param from character
	 * @param to character
	 * @return A language matching <code>[from-to]</code>
	 */
	public Node<Character,Character> range(char from, char to) {
		// Swap order if from is greater than to
		char tmp = from;
		if (from > to) {
			from = to;
			to = tmp;
		}
		int key = (from << 16) ^ to;
		return Node.createCached(rangeCache, key, Node.Tag.SYMBOL, from,to);
	}
	
	private Map<Long, Node<Node<?,?>,Node<?,?>>> listCache = new HashMap<Long, Node<Node<?,?>,Node<?,?>>>();
	private boolean skipDefinedIdentifiers = false;
	// Skip through defined identifiers
	private Node<?,?> getRHS(Node<?,?> language) {
		if (skipDefinedIdentifiers ) {
			int key = Node.id(language);
			if (Node.tag(language) == Node.Tag.ID && rules.containsKey(key)) {
				Node<Node<String,Void>,Node<?,?>> result = rules.get(key);
				return Node.right(result);
			}
		}
		return language;
	}
	// See: http://cs.brown.edu/people/jes/book/pdfs/ModelsOfComputation.pdf
	private Node<?,?> listInstance(Node<?,?> left, Node<?,?> right) {
		assert left != null;
		assert right != null;
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
		long key = ((long)Node.id(left) << 32) ^ Node.id(right);
		return Node.createCached(listCache, key, Node.Tag.LIST, left, right);
	}
	private Node<?,?> list(Node<?,?>[] nodes, int i) {
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
	public Node<?,?> list(Node<?,?>... sequence) {
		return list(sequence, 0);
	}
	
	// Convert a string into an array of symbols
	private Node<?,?>[] explode(String string) {
		Node<?,?>[] array = new Node<?,?>[string.length()];
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
	public Node<?,?> string(String string) {
		return list(explode(string), 0);
	}
	private Map<Long, Node<Node<?,?>,Node<?,?>>> setCache = new HashMap<Long, Node<Node<?,?>,Node<?,?>>>();

	private Node<?,?> orInstance(Node<?,?> left, Node<?,?> right) {
		// Skip through defined identifiers
		left = getRHS(left);
		right = getRHS(right);
		// r+0 = 0+r = r
		if (left == reject) { return right; }
		if (right == reject) { return left; }
		// r+r = r
		if (left == right) { return left; }
		// r+(s+r) = (r+s)+r = r+(r+s) = (s+r)+r = r+s
		if (Node.tag(left) == Node.Tag.SET) {
			Node<?,?> l = (Node<?,?>) left;
			if (Node.left(l) == right || Node.right(l) == right) return l;
		}
		if (Node.tag(right) == Node.Tag.SET) {
			Node<?,?> r = (Node<?,?>) right;
			if (Node.left(r) == left || Node.right(r) == left) return r;
		}
		// Ensure a canonical order for sets
		Node<?,?> tmp = left;
		if (Node.id(left) > Node.id(right)) {
			left = right;
			right = tmp;
		}
		long key = ((long)Node.id(left) << 32) ^ Node.id(right);
		return Node.createCached(setCache, key, Node.Tag.SET, left, right);
	}
	private Node<?,?> or(Node<?,?>[] nodes, int i) {
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
	public Node<?,?> or(Node<?,?>... options) {
		return or(options, 0);
	}
	/**
	 * Match one of the characters in a string.
	 * @param string of characters
	 * @return A language matching one of the characters in the string.
	 */
	public Node<?,?> oneOf(String string) {
		return or(explode(string), 0);
	}
	/**
	 * Match a sequence, optionally.
	 * 
	 * @param sequence to match optionally (a,b,c,...,z)
	 * @return A language matching the sequence optionally <code>(abc...z)?</code>
	 */
	public Node<?,?> option(Node<?,?>... sequence) {
		return or(empty, list(sequence));
	}
	
	private Map<Integer, Node<Node<?,?>,Node<?,?>>> loopCache = new HashMap<Integer, Node<Node<?,?>,Node<?,?>>>();
	/**
	 * Match a sequence zero or more times.
	 * 
	 * @param sequence to match repeatedly (e.g., <code>ab</code>)
	 * @return A language matching the sequence zero or more times: <code>(ab)*</code>
	 */
	public Node<?,?> many(Node<?,?>... sequence) {
		Node<?,?> language = list(sequence);
		// 0* = e* = e
		if (language == empty || language == reject) { return empty; }
		if (Node.tag(language) == Node.Tag.LOOP) return language;
		return Node.createCached(loopCache, Node.id(language), Node.Tag.LOOP, language, any);
	}
	/**
	 * Matches zero or more occurrences of language, separated by separator.
	 * 
	 * @param language to match
	 * @param separator to use
	 * @return
	 */
	public Node<?,?> sepBy(Node<?,?> language, Node<?,?> separator) {
		return null;
	}
	/**
	 * Matches one or more occurrences of language, separated by separator.
	 * @return
	 */
	public Node<?,?> sepBy1(Node<?,?> language, Node<?,?> separator) {
		return null;
	}
	/**
	 * Match a sequence one or more times.
	 * @param sequence to match repeatedly (e.g., <code>ab</code>)
	 * @return A language matching the sequence one or more times: <code>(ab)+</code>
	 */
	public Node<?,?> many1(Node<?,?>... sequence) {
		return list(list(sequence), many(sequence));
	}
	// Identifier lookup by name
	private Map<String, Node<String,Void>> labels = new HashMap<String, Node<String,Void>>();
	private Set<Node<String,Void>> ids = new LinkedHashSet<Node<String,Void>>();
	/**
	 * Create or use an identifier (a terminal or nonterminal variable).
	 * 
	 * @param label for the identifier.
	 * @return The identifier.
	 */
	public Node<String,Void> id(String label) {
		assert label != null;
		return Node.createCached(labels, label, Node.Tag.ID, label, (Void)null);
	}
	/**
	 * Create an identifier (a terminal or nonterminal variable).
	 * @return The identifier
	 */
	public Node<String,Void> id() {
		Node<String,Void> result = Node.create(Node.Tag.ID, (String)null, (Void)null);
		ids.add(result);
		return result;
	}
	/** The language definition. The root of all traversal. */
	private Node<?,?> definition = reject;
	/**
	 * Get the language definition
	 * @return the root/start node in the language data structure.
	 */
	public Node<?,?> definition() {
		return definition;
	}
	/** Tokenization separator */
	private Node<?,?> separator = empty;
	
	Map<Integer, Node<Node<String,Void>, Node<?,?>>> rules = new HashMap<Integer, Node<Node<String,Void>, Node<?,?>>>();
	
	Map<Integer, Node<String,Void>> reverse = new HashMap<Integer, Node<String,Void>>();
	private Node<?,?> undefine(Node<String,Void> id) {
		ids.remove(id);
		rules.remove(Node.id(id));
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
	public Node<?,?> rule(String label, Node<?,?>... rhs) {
		assert label != null;
		assert rhs != null;
		Node<?,?> result = rule(id(label), rhs);
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
	public Node<?,?> rule(Node<String,Void> id, Node<?,?>... rhs) {
		assert id != null;
		assert rhs != null;
		Node<?,?> right = list(rhs);
		right = getRHS(right);
		// If the right rejects, or Id -> Id literally, remove the identifier and reject
		if (right == reject || id == right) {
//			return undefine(id);
			return reject;
		}
		// If the right hand side is just an undefined identifier, don't create a rule, just return the existing identifier
		int key = Node.id(id);
		if (Node.tag(right) == Node.Tag.ID && !rules.containsKey(key)) {
//			undefine(id);
			return right;
		}
		// If we defined this language already with a different identifier, return the existing identifier
		if (reverse.containsKey(Node.id(right))) {
			Node<String,Void> storedId = reverse.get(Node.id(right));
			if (rules.containsKey(Node.id(storedId)) && !rules.containsKey(key)) {
				return storedId;
			}
		}
		reverse.put(Node.id(right), id);
		Node<Node<String,Void>,Node<?,?>> node = Node.createCached(rules, key, Node.Tag.RULE, id, right);
		assert Node.left(node) == id;
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
	public Node<?,?> token(Node<String,Void> id, Node<?,?>... sequence) {
		return rule(id, list(sequence));
	}
	
	/**
	 * Define the separator for tokenization.
	 * 
	 * @param separator for tokenization
	 * @return the language
	 */
	public Node<?,?> separator(Node<?,?>... separator) {
		return this.separator = list(separator);
	}

	/**
	 * Specify a language.
	 * <p>
	 * For regular expressions, surround the definition with <code>define()</code>.
	 * For grammars, the first derivation is the starting nonterminal.
	 * 
	 * @param language The language
	 */
	public void define(Node<?,?>... language) {
		definition = list(language);
	}
	/**
	 * Accept visitor into a rule of the form: <code>id -> right</code>
	 * 
	 * @param visitor
	 * @param id
	 * @return
	 */
	public <T> T acceptRule(Visitor<T> visitor, Node<String,Void> id) {
		assert id != null;
		visitor.getWorkList().done(id);
		// Defined identifiers
		if (rules.containsKey(Node.id(id))) {
			return visitor.rule((Node<Node<String,Void>,Node<?,?>>)rules.get(Node.id(id)));
		}
		// Undefined identifiers
		return visitor.reject(reject);
	}
	public final Compute get = new Compute(this);
	public String toString() {
		return get.printer.compute().toString();
	}
}
