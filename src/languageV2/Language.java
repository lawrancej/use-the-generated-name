package languageV2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import languageV2.traversal.*;
import util.*;

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
	/** The language constructs (set, list, symbol, loop, id) */
	private static final Construct[] constructs = Construct.values();
	/** Match any character, equivalent to regular expression dot. */
	public static final Node<Character> any = Node.create(Construct.SYMBOL.ordinal(), null);
	/** Match the empty list (empty sequence). */
	public static final Node<TaggedDataPair> empty = Node.create(Construct.LIST.ordinal(), null);
	/** Reject everything (the empty set). */
	public static final Node<TaggedDataPair> reject = Node.create(Construct.SET.ordinal(),null);

	private Map<Character, Node<?>> symbols = new HashMap<Character, Node<?>>();
	/**
	 * Match a character
	 * 
	 * @param c The character
	 * @return A language matching character c.
	 */
	public Node<?> symbol(char c) {
		if (!symbols.containsKey(c)) {
			Node<?> result = Node.create(Construct.SYMBOL.ordinal(), c);
			symbols.put(c, result);
			return result;
		}
		return symbols.get(c);
	}
	public Node<?> range(char from, char to) {
		Node<?>[] symbols = new Node<?>[to - from];
		for (char i = from; i < to; i++) {
			symbols[i-from] = symbol(i);
		}
		return or(symbols, 0);
	}
	
	private Map<Integer, Node<?>> listCache = new HashMap<Integer, Node<?>>();
	// See: http://cs.brown.edu/people/jes/book/pdfs/ModelsOfComputation.pdf
	private Node<?> listInstance(Node<?> left, Node <?> right) {
		// (1) r0 = 0r = 0
		if (left == reject || right == reject) {
			return reject;
		}
		// (2) re = er = r
		if (left == empty) { return right; }
		if (right == empty) { return left; }
		// (8) r(st) = (rs)t (FIXME: test to ensure list structures are enforced)
		// FIXME: this is fast, but a bit dodgy
		int key = left.hashCode() ^ right.hashCode();
		if (!listCache.containsKey(key)) {
			Node<?> result = Node.create(Construct.LIST.ordinal(), new TaggedDataPair(left, right));
			listCache.put(key, result);
			return result;
		}
		return listCache.get(key);
	}
	private Node<?> list(Node<?>[] nodes, int i) {
		if (i >= nodes.length) {
			return empty;
		} else {
			return listInstance(nodes[i], list(nodes, i+1));
		}
	}
	/**
	 * Match a sequence of languages
	 * 
	 * @param nodes A sequence of languages
	 * @return A language matching the languages in order
	 */
	public Node<?> list(Node<?>... nodes) {
		return list(nodes, 0);
	}
	/**
	 * Match String s literally.
	 * 
	 * @param s the String to match
	 * @return A language matching String s.
	 */
	public Node<?> string(String s) {
		Node<?>[] array = new Node<?>[s.length()];
		for (int i = 0; i < s.length(); i++) {
			array[i] = symbol(s.charAt(i));
		}
		return list(array, 0);
	}
	private Map<Integer, Node<?>> setCache = new HashMap<Integer, Node<?>>();

	private Node<?> orInstance(Node<?> left, Node <?> right) {
		// (3) r+0 = 0+r = r
		if (left == reject) { return right; }
		if (right == reject) { return left; }
		// (4) r+r = r
		if (left == right) { return left; }
		// (5) r+s = s+r (No need to sort regexes to ensure canonical order)
		// (6) r(s+t) = rs+rt (FIXME: factor out common prefixes)
		// (7) (r+s)t = rt+st (FIXME: factor out common suffixes)
		int key = left.hashCode() ^ right.hashCode();
		if (!setCache.containsKey(key)) {
			Node<?> result = Node.create(Construct.SET.ordinal(), new TaggedDataPair(left, right));
			setCache.put(key, result);
			return result;
		}
		return setCache.get(key);
	}
	private Node<?> or(Node<?>[] nodes, int i) {
		if (i >= nodes.length) {
			return reject;
		} else {
			return orInstance(nodes[i], or(nodes, i+1));
		}
	}
	/**
	 * Match one of the languages
	 * 
	 * @param nodes Languages to match
	 * @return A language matching one of the languages
	 */
	public Node<?> or(Node<?>... nodes) {
		return or(nodes, 0);
	}
	/**
	 * Match a language, optionally.
	 * @param language The language to match
	 * @return A language matching one of the languages, optionally.
	 */
	public Node<?> option(Node<?> language) {
		return or(language, empty);
	}
	
	/**
	 * Match a language zero or more times
	 * 
	 * @param language The language
	 * @return A language that matches the input language zero or more times
	 */
	public Node<?> many(Node<?>... nodes) {
		Node<?> language = list(nodes);
		// Avoid creating a new loop, if possible
		// (9) 0* = e
		// (10) e* = e
		// (11) (e+r)+ = r* (FIXME: need plus loop)
		// (12) (e+r)* = r* (FIXME: check for this condition)
		// (13) r*(e+r) = (e+r)r* = r* (FIXME: check for this condition)
		// (14) r*s+s = r*s (FIXME: check for this condition)
		// (15) r(sr)* = (rs)*r (FIXME: check for this condition)
		// (16) (r+s)* = (r*s)*r* = (s*r)*s* (FIXME: check for this condition)
		if (language == empty || language == reject) { return empty; }
		if (constructs[language.tag] == Construct.LOOP) return language;
		return Node.create(Construct.LOOP.ordinal(), language);
	}
	/** Identifiers are nonterminals. Identifiers enable recursion. */
	public static class Id extends Node<String> {
		public Id() {
			super(Construct.ID.ordinal(), null);
		}
		public Id(String label) {
			super(Construct.ID.ordinal(), label);
		}
		private Node<?> rhs = reject;
	}
	// Identifier lookup by name
	private Map<String, Id> labels = new HashMap<String, Id>();
	private Set<Id> ids = new HashSet<Id>();
	/**
	 * Reference an identifier.
	 * 
	 * @param s The identifier name.
	 * @return The identifier.
	 */
	public Id id(String s) {
		if (!labels.containsKey(s)) {
			labels.put(s, new Id(s));
		}
		return labels.get(s);
	}
	public Id id() {
		Id result = new Id();
		ids.add(result);
		return result;
	}
	
	/** The language definition. The root of all traversal. */
	private Node<?> definition = reject;
	/**
	 * Define an identifier: `id -> rhs`
	 * 
	 * If the list of languages rejects, then this removes the identifier.
	 * 
	 * @param id the identifier
	 * @param languages the right hand side
	 * @return the identifier reference
	 */
	public Node<?> derives(String id, Node<?>... languages) {
		Node<?> result = derives(id(id), languages);
		if (result == reject) {
			labels.remove(id);
		}
		return result;
	}
	
	public Node<?> derives(Id id, Node<?>... languages) {
		Id result = id;
		Node<?> rhs = list(languages);
		// If the rhs rejects, remove identifier
		if (rhs == reject) {
			ids.remove(id);
			return reject;
		}
		// If the rhs is just an identifier...
		if (constructs[rhs.tag] == Construct.ID && result.rhs == reject) {
			ids.remove(id);
			return rhs;
		}
		// If the language is undefined, make this the starting nonterminal
		if (definition == reject) {
			definition = result;
		}
		result.rhs = or(result.rhs, rhs);
		return result;
	}
	
	/**
	 * Specify a language.
	 * 
	 * For regular expressions, surround the definition with define().
	 * For grammars, the first derivation is the starting nonterminal.
	 * 
	 * @param language The language
	 */
	public void define(Node<?> language) {
		definition = language;
	}
	
	/**
	 * Accept visitor into a rule of the form id ::= rhs.
	 * 
	 * @param visitor
	 * @param id
	 * @return
	 */
	public <T> T acceptRule(Visitor<T> visitor, Id id) {
		visitor.getWorkList().done(id);
		return visitor.rule(id, id.rhs);
	}
	/**
	 * Accept visitor into a language.
	 * 
	 * @param visitor
	 * @param language
	 * @return
	 */
	public <T> T accept(Visitor<T> visitor, Node<?> language) {
		switch(constructs[language.tag]) {
		case ID:
			visitor.getWorkList().todo((Id)language);
			return visitor.id((Id)language);
		case LIST:
			return visitor.list((Node<TaggedDataPair>)language);
		case LOOP:
			return visitor.loop((Node<Node<?>>)language);
		case SET:
			return visitor.set((Node<TaggedDataPair>)language);
		case SYMBOL:
			return visitor.symbol((Node<Character>)language);
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
	public <T> T beginTraversal(Visitor<T> visitor, Node<?> language) {
		visitor.getWorkList().clear();
		visitor.begin();
		T accumulator;
		// Visit a grammar
		if (language.tag == Construct.ID.ordinal()) {
			visitor.getWorkList().todo((Id)language);
			accumulator = visitor.bottom();
			for (Id identifier : visitor.getWorkList()) {
				accumulator = visitor.reduce(accumulator, acceptRule(visitor, identifier));
				if (visitor.done(accumulator)) {
					visitor.end();
					return accumulator;
				}
			}
		}
		// Visit a regex
		else {
			accumulator = accept(visitor, language);
		}
		visitor.end();
		return accumulator;
	}
	/**
	 * Begin traversal of the language specification
	 * @param visitor
	 * @return
	 */
	public <T> T beginTraversal(Visitor<T> visitor) {
		return beginTraversal(visitor, definition);
	}
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
	public String toString(Node<?> language) {
		return beginTraversal(new Printer(this), language).toString();
	}
	/**
	 * Compute the first set for identifier s.
	 * 
	 * @param id the identifier
	 * @return The first set for the identifier.
	 */
	public Node<?> first(String id) {
		return beginTraversal(new FirstSet(this), id);
	}
	/**
	 * Compute the first set for the language specification.
	 * 
	 * @return The first set (the set of symbols appearing first in any derivation)
	 */
	public Node<?> first() {
		return beginTraversal(new FirstSet(this));
	}
	
	private Nullable nullable = new Nullable(this);
	/**
	 * Can this language derive the empty string?
	 * 
	 * @param language
	 */
	public boolean nullable(Node<?> language) {
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
	public Node<?> derivative(char c, Node<?> language) {
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
	public Node<?> derivative(char c) {
		return derivative(c, definition);
	}
	
	/**
	 * Garbage collect unreferenced identifiers (nonterminals).
	 * @param language the root set language for gc.
	 */
	public void gc(Node<?> language) {
		WorkQueue<Id> list = derivative.getWorkList();
		Iterator<Entry<String, Id>> iterator = labels.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, Id> entry = iterator.next();
			Id id = entry.getValue();
			if (!list.visited(id)) {
				iterator.remove();
				ids.remove(id);
			}
		}
	}
	/**
	 * Garbage collect unreferenced identifiers (nonterminals).
	 */
	public void gc() {
		gc(definition);
	}
	
	// FIXME: this code will be unnecessary once we switch to using persistent sets (aka treaps)
	
	private Map<String, Id> startids = new HashMap<String, Id>();
	private Set<Id> startidSet = new HashSet<Id>();
	
	public void backup() {
		for (Id id : labels.values()) {
			startids.put((String)id.data, id);
		}
		for (Id id : ids) {
			startidSet.add(id);
		}
	}
	public void restore() {
		labels.clear();
		ids.clear();
		labels = startids;
		ids = startidSet;
	}

	public boolean matches(Node<?> language, String s) {
		boolean result;
		backup();
		GraphViz gv = new GraphViz(this);
		for (int i = 0; i < s.length(); i++) {
			language = derivative(s.charAt(i), language);
			gc(language);
			if (debug) {
				if (labels.size() > 0) {
//					System.out.println("top: " + (String)language.data);
//					System.out.println("ids: " + labels.size() + " " + labels.keySet());
//					System.out.println(toString(language));
					System.out.println(beginTraversal(gv, language));
				}
//				System.out.println(s.charAt(i));
			}
		}
		if (debug) {
			System.out.println(beginTraversal(gv, language));
//			System.out.println(toString(language));
		}
		result = nullable(language);
		restore();
		return result;
	}
	public boolean matches(String s) {
		return matches(definition, s);
	}
}
