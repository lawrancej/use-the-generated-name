package languageV2;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import languageV2.traversal.*;

/**
 * A language specification
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
		if (language.tag == Node.Tag.ID && language.right != reject) {
			ids.remove(language);
			language = (Node<?, ?>) language.right;
		}
		return language;
	}
	// See: http://cs.brown.edu/people/jes/book/pdfs/ModelsOfComputation.pdf
	private Node<?,?> listInstance(Node<?,?> left, Node<?,?> right) {
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
	private Node<?,?> list(Node<?,?>[] nodes, int i) {
		if (i >= nodes.length) {
			return empty;
		} else {
			return listInstance(nodes[i], list(nodes, i+1));
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
	/**
	 * Match a string literally.
	 * 
	 * @param string to match literally (e.g., <code>hello</code>)
	 * @return A language matching the string: <code>hello<code>
	 */
	public Node<?,?> string(String string) {
		Node<?,?>[] array = new Node<?,?>[string.length()];
		for (int i = 0; i < string.length(); i++) {
			array[i] = symbol(string.charAt(i));
		}
		return list(array, 0);
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
		Id loop = id();
		boolean flag = (definition == reject);
		derives(loop, option(language, loop));
		if (flag) {
			definition = reject;
		}
		return loop;
	}
	/** Identifiers are terminal or nonterminal variables. */
	public static class Id extends Node<String,Node<?,?>> {
		public Id() {
			super(Node.Tag.ID, null, reject);
		}
		public Id(String label) {
			super(Node.Tag.ID, label, reject);
		}
	}
	// Identifier lookup by name
	private Map<String, Id> labels = new HashMap<String, Id>();
	private Set<Id> ids = new HashSet<Id>();
	/**
	 * Create or use an identifier (a terminal or nonterminal variable).
	 * 
	 * @param label for the identifier.
	 * @return The identifier.
	 */
	public Id id(String label) {
		if (!labels.containsKey(label)) {
			labels.put(label, new Id(label));
		}
		return labels.get(label);
	}
	/**
	 * Create an identifier (a terminal or nonterminal variable).
	 * @return The identifier
	 */
	public Id id() {
		Id result = new Id();
		ids.add(result);
		return result;
	}
	
	/** The language definition. The root of all traversal. */
	private Node<?,?> definition = reject;
	/**
	 * Define an identifier.
	 * 
	 * <p>
	 * <code>label -> rhs</code>
	 * <p>
	 * Side effect: the first call to <code>derives</code> defines the starting identifier.
	 * Subsequent calls to <code>derives</code> add rules for the identifier.
	 * 
	 * @param label for the identifier
	 * @param rhs the right hand side
	 * @return The identifier, or reject (if the rhs rejects or <code>id -> id</code>).
	 */
	public Node<?,?> derives(String label, Node<?,?>... rhs) {
		Node<?,?> result = derives(id(label), rhs);
		if (result == reject) {
			labels.remove(label);
		}
		return result;
	}
	/**
	 * Define an identifier.
	 * 
	 * <p>
	 * <code>id -> rhs</code>
	 * <p>
	 * Side effect: the first call to <code>derives</code> defines the starting identifier.
	 * Subsequent calls to <code>derives</code> add rules for the identifier.
	 * 
	 * @param id the identifier
	 * @param rhs the right hand side
	 * @return The identifier, or reject (if the rhs rejects or <code>id -> id</code>).
	 */
	public Node<?,?> derives(Id id, Node<?,?>... rhs) {
		Id result = id;
		Node<?,?> right = list(rhs);
		// If the right rejects, remove the identifier
		if (right == reject) {
			ids.remove(id);
			return reject;
		}
		// If we defined this language already with a different identifier, return the existing identifier
		if (right.tag == Node.Tag.ID && result.right == reject) {
			ids.remove(id);
			return right;
		}
		// If Id -> Id literally, reject
		if (id == right) {
			ids.remove(id);
			return reject;
		}
		// If the language is undefined, make this the starting nonterminal
		if (definition == reject) {
			definition = result;
		}
		result.right = or(result.right, right);
		return result;
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
	public <T> T acceptRule(Visitor<T> visitor, Id id) {
		visitor.getWorkList().done(id);
		return visitor.rule(id, id.right);
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
			visitor.getWorkList().todo((Id)language);
			return visitor.id((Id)language);
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
		visitor.getWorkList().clear();
		visitor.begin();
		T accumulator;
		// Visit a grammar
		if (language.tag == Node.Tag.ID) {
			visitor.getWorkList().todo((Id)language);
			accumulator = visitor.bottom();
			for (Id identifier : visitor.getWorkList()) {
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
	public String toString() {
		return beginTraversal(new Printer(this)).toString();
	}
	/**
	 * Returns a string representation of this identifier.
	 * @param id an identifier
	 * @return a string representation of the identifier
	 */
	public String toString(String id) {
		return beginTraversal(new Printer(this), id).toString();
	}
	public String toString(Node<?,?> language) {
		return beginTraversal(new Printer(this), language).toString();
	}
	/**
	 * Compute the first set for identifier s.
	 * 
	 * @param id the identifier
	 * @return The first set for the identifier.
	 */
	public Node<?,?> first(String id) {
		return beginTraversal(new FirstSet(this), id);
	}
	/**
	 * Compute the first set for the language specification.
	 * 
	 * @return The first set (the set of symbols appearing first in any derivation)
	 */
	public Node<?,?> first() {
		return beginTraversal(new FirstSet(this));
	}
	
	private Nullable nullable = new Nullable(this);
	/**
	 * Can this language derive the empty string?
	 * 
	 * @param language
	 */
	public boolean nullable(Node<?,?> language) {
		return beginTraversal(nullable, language);
	}
	/**
	 * Can this identifier derive the empty string?
	 * 
	 * @param id
	 */
	public boolean nullable(String id) {
		return beginTraversal(nullable, id);
	}
	/**
	 * Can this language specification derive the empty string?
	 */
	public boolean nullable() {
		return beginTraversal(nullable);
	}
	
	private Derivative derivative = new Derivative(this);
	/**
	 * Compute the derivative of a language with respect to a character.
	 * 
	 * @param c
	 * @param language
	 * @return
	 */
	public Node<?,?> derivative(char c, Node<?,?> language) {
		derivative.c = c;
		return beginTraversal(derivative, language);
	}
	/**
	 * Compute the derivative of a language specification
	 * 
	 * @param c
	 * @param language
	 * @return
	 */
	public Node<?,?> derivative(char c) {
		return derivative(c, definition);
	}
	public boolean matches(Node<?,?> language, String s) {
		boolean result;
		GraphViz gv = new GraphViz(this);
		if (debug) {
			System.out.println(beginTraversal(gv, language));
			System.out.format("Nodes %d, edges %d\n", gv.nodes(), gv.edges());
		}
		for (int i = 0; i < s.length(); i++) {
			language = derivative(s.charAt(i), language);
			if (debug) {
				beginTraversal(gv, language);
				System.out.format("Nodes %d, edges %d\n", gv.nodes(), gv.edges());
			}
		}
		if (debug) {
			System.out.println(beginTraversal(gv, language));
			System.out.println(toString(language));
		}
		result = nullable(language);
		return result;
	}
	public boolean matches(String s) {
		return matches(definition, s);
	}
}
