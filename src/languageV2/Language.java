package languageV2;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import languageV2.traversal.*;

/**
 * Specify a language, and accept visitors into the language specification.
 * 
 * @author Joseph Lawrance
 *
 */
@SuppressWarnings("unchecked")
public class Language {
	/** Construct a language specification */
	public Language() {}
	/** Match any character, equivalent to regular expression dot. */
	public static final Node<Character,Character> any = Node.create(Node.Tag.SYMBOL, null, null);
	/** Match the empty list (empty sequence). */
	public static final Node<Node<?,?>,Node<?,?>> empty = Node.create(Node.Tag.LIST, null,null);
	/** Reject everything (the empty set). */
	public static final Node<Node<?,?>,Node<?,?>> reject = Node.create(Node.Tag.SET,null,null);

	private Map<Integer, Node<Character,Character>> alphabet = new HashMap<Integer, Node<Character,Character>>();
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
		char tmp = from;
		if (from > to) {
			from = to;
			to = tmp;
		}
		int key = (from << 16) | to;
		if (!alphabet.containsKey(key)) {
			Node<Character,Character> result = Node.create(Node.Tag.SYMBOL, from,to);
			alphabet.put(key, result);
			return result;
		}
		return alphabet.get(key);
	}
	
	private Map<Integer, Node<Node<?,?>,Node<?,?>>> listCache = new HashMap<Integer, Node<Node<?,?>,Node<?,?>>>();
	private Node<?,?> skipDefinedIdentifier(Node<?,?> language) {
		int key = language.hashCode();
		if (language.tag == Node.Tag.ID && rules.containsKey(key)) {
			ids.remove(language);
			language = (Node<?, ?>) rules.get(key).right;
		}
		return language;
	}
	// See: http://cs.brown.edu/people/jes/book/pdfs/ModelsOfComputation.pdf
	private Node<?,?> listInstance(Node<?,?> left, Node<?,?> right) {
		assert left != null;
		assert right != null;
		// Skip through defined identifiers
		left = skipDefinedIdentifier(left);
		right = skipDefinedIdentifier(right);
		// r0 = 0r = 0
		if (left == reject || right == reject) {
			return reject;
		}
		// re = er = r
		if (left == empty) { return right; }
		if (right == empty) { return left; }
		// FIXME: this is fast, but a bit dodgy
		int key = left.hashCode() ^ right.hashCode();
		if (!listCache.containsKey(key)) {
			Node<?,?> result = Node.create(Node.Tag.LIST, left, right);
			listCache.put(key, (Node<Node<?, ?>, Node<?, ?>>) result);
			return result;
		}
		return listCache.get(key);
	}
	private Node<?,?> list(Node<?,?>[] nodes, int i, Node<?,?> separator) {
		if (i >= nodes.length) {
			return empty;
		} else {
			// return listInstance(nodes[i], list(nodes, i+1));
			return listInstance(separator, listInstance(nodes[i], list(nodes, i+1, separator)));
		}
	}
	/**
	 * Match a sequence of languages.
	 * 
	 * @param sequence of languages (a,b,c,...)
	 * @return A language matching the sequence in order: <code>abcd...</code>
	 */
	public Node<?,?> list(Node<?,?>... sequence) {
		return list(sequence, 0, empty);
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
		return list(explode(string), 0, empty);
	}
	private Map<Integer, Node<?,?>> setCache = new HashMap<Integer, Node<?,?>>();

	private Node<?,?> orInstance(Node<?,?> left, Node<?,?> right) {
		// Skip through defined identifiers
		left = skipDefinedIdentifier(left);
		right = skipDefinedIdentifier(right);
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
		int key = left.hashCode() ^ right.hashCode();
		if (!setCache.containsKey(key)) {
			Node<?,?> result = Node.create(Node.Tag.SET, left, right);
			setCache.put(key, result);
			return result;
		}
		return setCache.get(key);
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
	
	/**
	 * Match a sequence zero or more times.
	 * 
	 * @param sequence to match repeatedly (e.g., <code>ab</code>)
	 * @return A language matching the sequence zero or more times: <code>(ab)*</code>
	 */
	public Node<?,?> many(Node<?,?>... sequence) {
		Node<?,?> language = list(sequence);
		// Skip through defined identifiers
		language = skipDefinedIdentifier(language);
		// Avoid creating a new loop, if possible
		// 0* = e* = e
		if (language == empty || language == reject) { return empty; }
		Node<String,Void> loop = id();
		boolean flag = (definition == reject);
		rule(loop, option(language, loop));
		if (flag) {
			definition = reject;
		}
		return loop;
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
	private Set<Node<String,Void>> ids = new HashSet<Node<String,Void>>();
	/**
	 * Create or use an identifier (a terminal or nonterminal variable).
	 * 
	 * @param label for the identifier.
	 * @return The identifier.
	 */
	public Node<String,Void> id(String label) {
		assert label != null;
		if (!labels.containsKey(label)) {
			labels.put(label, Node.create(Node.Tag.ID, label, (Void)null));
		}
		return labels.get(label);
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
			labels.remove(label);
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
		// If the right rejects, remove the identifier
		if (right == reject) {
			ids.remove(id);
			return reject;
		}
		// If we defined this language already with a different identifier, return the existing identifier
		int key = id.hashCode();
		if (right.tag == Node.Tag.ID && !rules.containsKey(key)) {
			ids.remove(id);
			return right;
		}
		// If Id -> Id literally, reject
		if (id == right) {
			ids.remove(id);
			return reject;
		}
		// If the language is undefined, make this the starting identifier
		if (definition == reject) {
			definition = id;
		}
		if (!rules.containsKey(key)) {
			Node<Node<String, Void>, ?> rule = Node.create(Node.Tag.RULE, id, right);
			rules.put(key, rule);
			return id;
		}
		return rules.get(key);
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
	Map<Integer, Node<Node<String,Void>, ?>> rules = new HashMap<Integer, Node<Node<String,Void>, ?>>();
	/**
	 * Accept visitor into a rule of the form: <code>id -> right</code>
	 * 
	 * @param visitor
	 * @param id
	 * @return
	 */
	public <T> T acceptRule(Visitor<T> visitor, Node<String,Void> id) {
		visitor.getWorkList().done(id);
		return visitor.rule((Node<Node<String,Void>,Node<?,?>>)rules.get(id.hashCode()));
	}
	/**
	 * Accept visitor into a language.
	 * 
	 * @param visitor
	 * @param language
	 * @return
	 */
	public <T> T accept(Visitor<T> visitor, Node<?,?> language) {
		switch(language.tag) {
		case ID:
			visitor.getWorkList().todo((Node<String,Void>)language);
			return visitor.id((Node<String,Void>)language);
		case LIST:
			return visitor.list((Node<Node<?,?>,Node<?,?>>)language);
		case SET:
			return visitor.set((Node<Node<?,?>,Node<?,?>>)language);
		case SYMBOL:
			return visitor.symbol((Node<Character,Character>)language);
		default:
			return null;
		}
	}
	/**
	 * Begin traversal of the language specification, at specified identifier
	 * @param visitor
	 * @param id
	 */
	public <T> T beginTraversal(Visitor<T> visitor, String id) {
		return beginTraversal(visitor, id(id));
	}
	/**
	 * Begin traversal of a language
	 * @param visitor
	 * @param language
	 * @return
	 */
	public <T> T beginTraversal(Visitor<T> visitor, Node<?,?> language) {
		assert visitor != null;
		assert language != null;
		visitor.getWorkList().clear();
		visitor.begin();
		T accumulator;
		// Visit a grammar
		if (language.tag == Node.Tag.ID) {
			visitor.getWorkList().todo((Node<String,Void>)language);
			accumulator = visitor.bottom();
			for (Node<String,Void> identifier : visitor.getWorkList()) {
				accumulator = visitor.reduce(accumulator, acceptRule(visitor, identifier));
				if (visitor.done(accumulator)) {
					return visitor.end(accumulator);
				}
			}
		}
		// Visit a regex
		else {
			accumulator = accept(visitor, language);
		}
		return visitor.end(accumulator);
	}
	/**
	 * Begin traversal of the language specification
	 * @param visitor
	 * @return
	 */
	public <T> T beginTraversal(Visitor<T> visitor) {
		return beginTraversal(visitor, definition);
	}
	/**
	 * Debug output
	 */
	public boolean debug = false;
	public final Compute get = new Compute(this);
	public String toString() {
		return get.printer.compute().toString();
	}
}
