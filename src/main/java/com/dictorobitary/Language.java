package com.dictorobitary;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Specify a language.
 * 
 * In OO terms, this is a Node factory.
 * This Node factory performs compaction by construction.
 * 
 * @author Joseph Lawrance
 *
 */
public class Language {
	public final String name;
	/** Construct a language specification */
	public Language(String name) { this.name = name; }
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
	// Skip through defined identifiers
	private Node<?,?> getRHS(Node<?,?> language) {
//		long key = language.id;
//		if (language.tag == Node.Tag.ID && rules.containsKey(key)) {
//			return rules.get(key).right;
//		}
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
		long key = (left.id << 32) ^ right.id;
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
		if (left.tag == Node.Tag.SET) {
			Node<?,?> l = (Node<?,?>) left;
			if (l.left == right || l.right == right) return l;
		}
		if (right.tag == Node.Tag.SET) {
			Node<?,?> r = (Node<?,?>) right;
			if (r.left == left || r.right == left) return r;
		}
		// Ensure a canonical order for sets
		Node<?,?> tmp = left;
		if (left.id > right.id) {
			left = right;
			right = tmp;
		}
		long key = (left.id << 32) ^ right.id;
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
	
	private Map<Long, Node<Node<?,?>,Node<?,?>>> loopCache = new HashMap<Long, Node<Node<?,?>,Node<?,?>>>();
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
		if (language.tag == Node.Tag.LOOP) return language;
		return Node.createCached(loopCache, language.id, Node.Tag.LOOP, language, any);
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
	
	Map<Long, Node<Node<String,Void>, Node<?,?>>> rules = new HashMap<Long, Node<Node<String,Void>, Node<?,?>>>();
	
	Map<Long, Node<String,Void>> reverse = new HashMap<Long, Node<String,Void>>();
	private Node<?,?> undefine(Node<String,Void> id) {
		ids.remove(id);
		rules.remove(id.id);
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
			return undefine(id);
//			return reject;
		}
		// If the right hand side is a defined identifier, don't create a rule, just return the existing identifier
		long key = id.id;
		if (right.tag == Node.Tag.ID && !rules.containsKey(key)) {
			undefine(id);
			return right;
		}
		// If we defined this language already with a different identifier, return the existing identifier
		if (reverse.containsKey(right.id)) {
			Node<String,Void> storedId = reverse.get(right.id);
			if (rules.containsKey(storedId.id) && !rules.containsKey(key)) {
				return storedId;
			}
		}
		reverse.put(right.id, id);
		Node<Node<String,Void>,Node<?,?>> node = Node.createCached(rules, key, Node.Tag.RULE, id, right);
		assert node.left == id;
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
		if (rules.containsKey(id.id)) {
			return visitor.rule((Node<Node<String,Void>,Node<?,?>>)rules.get(id.id));
		}
		// Undefined identifiers
		return visitor.reject(reject);
	}
	public final Compute get = new Compute(this);
	public String toString() {
		return get.printer.compute().toString();
	}
}
