package languageV2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import languageV2.traversal.Derivative;
import languageV2.traversal.FirstSet;
import languageV2.traversal.GC;
import languageV2.traversal.Nonterminal;
import languageV2.traversal.Nullable;
import languageV2.traversal.Printer;
import languageV2.traversal.Visitor;
import languageV2.traversal.WorkQueue;
import util.TaggedData;
import util.TaggedDataCache;
import util.TaggedDataPair;

@SuppressWarnings("unchecked")
public class Language {
	/**
	 * Construct a language specification
	 */
	public Language() {}
	/** Symbols match a character. The null symbol matches any character. */
	public static final TaggedData<Character> any = TaggedData.create(Construct.SYMBOL.ordinal(), null);
	private TaggedDataCache<Character> symbols = TaggedDataCache.create(any);
	/**
	 * Match a character
	 * 
	 * @param c The character
	 * @return A language matching character c.
	 */
	public TaggedData<?> symbol(char c) {
		return symbols.getInstance(c);
	}
	
	/** Lists match a sequence. The null list matches the empty sequence. */
	public static final TaggedData<TaggedDataPair> empty = TaggedData.create(Construct.LIST.ordinal(), null);
	private TaggedDataCache<TaggedDataPair> lists = TaggedDataCache.create(empty);
	private TaggedData<?> listInstance(TaggedData<?> left, TaggedData <?> right) {
		// Avoid creating a new list, if possible
		if (left == reject || right == reject) {
			return reject;
		}
		if (left == empty) { return right; }
		if (right == empty) { return left; }
		TaggedDataPair pair = new TaggedDataPair(left, right);
		return lists.getInstance(pair);
	}
	private TaggedData<?> list(TaggedData<?>[] nodes, int i) {
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
	public TaggedData<?> list(TaggedData<?>... nodes) {
		return list(nodes, 0);
	}
	/**
	 * Match String s literally.
	 * 
	 * @param s the String to match
	 * @return A language matching String s.
	 */
	public TaggedData<?> string(String s) {
		TaggedData<?>[] array = new TaggedData<?>[s.length()];
		for (int i = 0; i < s.length(); i++) {
			array[i] = symbol(s.charAt(i));
		}
		return list(array, 0);
	}
	
	/** Sets match one of many possible options. The null set rejects. */
	public static final TaggedData<SetOfLanguages> reject = TaggedData.create(Construct.SET.ordinal(),null);
	private TaggedDataCache<SetOfLanguages> ors = TaggedDataCache.create(reject);
	// Get a set from the cache
	private TaggedData<?> setInstance(SetOfLanguages s) {
		return ors.getInstance(s);
	}
	// Set union
	private TaggedData<?> merge(TaggedData<SetOfLanguages> set, TaggedData<?> item) {
		if (set.data.contains(item)) {
			return set;
		} else {
			SetOfLanguages s = (SetOfLanguages)set.data.clone();
			s.add(item);
			return setInstance(s);
		}
	}
	// Set union
	private TaggedData<?> mergeAll(TaggedData<SetOfLanguages> set, TaggedData<SetOfLanguages> set2) {
		if (set.data.containsAll(set2.data)) {
			return set;
		} else {
			SetOfLanguages s = (SetOfLanguages)set.data.clone();
			s.addAll(set2.data);
			return setInstance(s);
		}
	}
	private TaggedData<?> orInstance(TaggedData<?> left, TaggedData <?> right) {
		if (left == reject) { return right; }
		if (right == reject) { return left; }
		if (left == right) { return left; }
		SetOfLanguages setOfLanguages;
		// Do the types differ?
		if (left.tag != right.tag) {
			if (Construct.values()[left.tag] == Construct.SET) {
				return merge((TaggedData<SetOfLanguages>)left, right);
			}
			else if (Construct.values()[right.tag] == Construct.SET) {
				return merge((TaggedData<SetOfLanguages>)right, left);
			}
		}
		// If they're both sets, merge them together
		else if (Construct.values()[left.tag] == Construct.SET) {
			return mergeAll((TaggedData<SetOfLanguages>)left, (TaggedData<SetOfLanguages>)right);
		}
		setOfLanguages = new SetOfLanguages();
		setOfLanguages.add(left);
		setOfLanguages.add(right);
		return setInstance(setOfLanguages);
	}
	private TaggedData<?> or(TaggedData<?>[] nodes, int i) {
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
	public TaggedData<?> or(TaggedData<?>... nodes) {
		return or(nodes, 0);
	}
	/**
	 * Match a language, optionally.
	 * @param language The language to match
	 * @return A language matching one of the languages, optionally.
	 */
	public TaggedData<?> option(TaggedData<?> language) {
		return or(language, empty);
	}
	
	/** Loops (Kleene stars) define repetition. */
	private TaggedDataCache<TaggedData<?>> stars = TaggedDataCache.create(new TaggedData<TaggedData<?>>(Construct.LOOP.ordinal(), null));
	/**
	 * Match a language zero or more times
	 * 
	 * @param language The language
	 * @return A language that matches the input language zero or more times
	 */
	public TaggedData<?> many(TaggedData<?> language) {
		assert language != null;
		// Avoid creating a new loop, if possible
		if (language == empty || language == reject) { return empty; }
		if (Construct.values()[language.tag] == Construct.LOOP) return language;
		return stars.getInstance(language);
	}
	
	/** Identifiers include terminals and nonterminals. Identifiers enable recursion. */
	// Identifier lookup by name
	private TaggedDataCache<String> ids = TaggedDataCache.create(new TaggedData<String>(Construct.ID.ordinal(), null));
	// Derivation (rhs) lookup by name
	private Map<String, TaggedData<?>> derivations = new HashMap<String, TaggedData<?>>();
	/**
	 * Reference an identifier (terminal or nonterminal).
	 * 
	 * @param s The identifier name.
	 * @return The identifier.
	 */
	public TaggedData<String> id(String s) {
		return ids.getInstance(s);
	}
	// Get the right hand side
	private TaggedData<?> rhs(String s) {
		if (!derivations.containsKey(s)) {
			return reject;
		}
		return derivations.get(s);
	}
	/** The language definition. The root of all traversal. */
	private TaggedData<?> definition = reject;
	/**
	 * Define an identifier: `id -> rhs`
	 * 
	 * @param id the identifier
	 * @param languages the right hand side
	 * @return the identifier reference
	 */
	public TaggedData<?> derives(String id, TaggedData<?>... languages) {
		TaggedData<?> result = id(id);
		if (definition == reject) {
			definition = result;
		}
//		TaggedData<?> rhs = list(languages);
//		if (rhs == reject && !ids.containsKey(id)) {
//			return reject;
//		}
		derivations.put(id, or(rhs(id), list(languages)));
		return result;
	}
	
	/**
	 * Specify a language.
	 * 
	 * For regular expressions, surround the definition with define().
	 * For grammars, call define() *after* specifying the language.
	 * 
	 * @param language The language
	 */
	public void define(TaggedData<?> language) {
		definition = language;
	}
	/**
	 * Specify a context-free language.
	 * 
	 * For grammars, call define() *after* specifying the language.
	 * @param id The starting identifier (nonterminal)
	 */
	public void define(String id) {
		definition = id(id);
	}
	
	/** Visitors traverse a tree. */
	/**
	 * Visit a rule of the form id ::= rhs.
	 * Use this method during traversal.
	 * @param visitor
	 * @param id
	 * @return
	 */
	public <T> T visit(Visitor<T> visitor, String id) {
		visitor.getWorkList().done(id);
		return visitor.rule(id, rhs(id));
	}
	/**
	 * Visit a language.
	 * Use this method during traversal.
	 * @param visitor
	 * @param language
	 * @return
	 */
	public <T> T visit(Visitor<T> visitor, TaggedData<?> language) {
		switch(Construct.values()[language.tag]) {
		case ID:
			visitor.getWorkList().todo((String)language.data);
			return visitor.id((String)language.data);
		case LIST:
			return visitor.list((TaggedDataPair)language.data);
		case LOOP:
			return visitor.loop((TaggedData<?>)language.data);
		case SET:
			return visitor.set((SetOfLanguages)language.data);
		case SYMBOL:
			return visitor.symbol((Character)language.data);
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
		visitor.getWorkList().todo(id);
		T accumulator = visitor.bottom();
		for (String identifier : visitor.getWorkList()) {
			accumulator = visitor.reduce(accumulator, visit(visitor, identifier));
			if (visitor.done(accumulator)) {
				return accumulator;
			}
		}
		return accumulator;
	}
	/**
	 * Begin traversal of a language
	 * @param visitor
	 * @param language
	 * @return
	 */
	public <T> T beginTraversal(Visitor<T> visitor, TaggedData<?> language) {
		// Visit a grammar
		if (language.tag == Construct.ID.ordinal()) {
			return beginTraversal(visitor, (String)language.data);
		}
		// Visit a regex
		else {
			return visit(visitor, language);
		}
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
	@Override
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
	public String toString(TaggedData<?> language) {
		return beginTraversal(new Printer(this), language).toString();
	}
	/**
	 * Is the identifier a nonterminal?
	 * @param s The identifier label
	 * @return Whether the identifier is a nonterminal
	 */
	public boolean isNonterminal(String s) {
		return beginTraversal(new Nonterminal(this, s), s);
	}
	/**
	 * Is the identifier a terminal?
	 * @param s The identifier label
	 * @return Whether the identifier is a terminal
	 */
	public boolean isTerminal(String s) {
		return !beginTraversal(new Nonterminal(this, s), s);
	}
	/**
	 * Compute the first set for identifier s.
	 * 
	 * @param id the identifier
	 * @return The first set for the identifier.
	 */
	public TaggedData<?> first(String id) {
		return beginTraversal(new FirstSet(this), id);
	}
	/**
	 * Compute the first set for the language specification.
	 * 
	 * @return The first set (the set of symbols appearing first in any derivation)
	 */
	public TaggedData<?> first() {
		return beginTraversal(new FirstSet(this));
	}
	
	/**
	 * Can this language derive the empty string?
	 * 
	 * @param language
	 */
	public boolean nullable(TaggedData<?> language) {
		return beginTraversal(new Nullable(this), language);
	}
	/**
	 * Can this identifier derive the empty string?
	 * 
	 * @param id
	 */
	public boolean nullable(String id) {
		return beginTraversal(new Nullable(this), id);
	}
	/**
	 * Can this language specification derive the empty string?
	 */
	public boolean nullable() {
		return beginTraversal(new Nullable(this));
	}
	// Compute the derivative of a language
	private TaggedData<?> derivative(Derivative visitor, char c, TaggedData<?> language) {
		visitor.c = c;
		visitor.getWorkList().clear();
		return beginTraversal(visitor, language);
	}
	public TaggedData<?> derivative(char c, TaggedData<?> language) {
		return derivative(new Derivative(this), c, language);
	}
	public TaggedData<?> derivative(char c) {
		return derivative(c, definition);
	}
	public void gc(TaggedData<?> language) {
		GC collector = new GC(this);
		beginTraversal(collector, language);
		WorkQueue<String> list = collector.getWorkList();
		Iterator<Entry<String, TaggedData<String>>> iterator = ids.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, TaggedData<String>> entry = iterator.next();
			if (!list.visited(entry.getKey())) {
				iterator.remove();
			}
		}
		Iterator<Entry<String, TaggedData<?>>> it = derivations.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, TaggedData<?>> entry = it.next();
			if (!list.visited(entry.getKey())) {
				it.remove();
			}
		}
	}
	public void gc() {
		gc(definition);
	}

	public boolean matches(TaggedData<?> language, String s) {
		boolean result;
		TaggedDataCache<String> startids = TaggedDataCache.create(new TaggedData<String>(Construct.ID.ordinal(), null));
		Map<String, TaggedData<?>> startderivations = new HashMap<String, TaggedData<?>>();
		for (TaggedData<?> id : ids.values()) {
			startids.getInstance((String)id.data);
			startderivations.put((String)id.data, rhs((String)id.data));
		}
		Derivative visitor = new Derivative(this);
		for (int i = 0; i < s.length(); i++) {
			language = derivative(visitor, s.charAt(i), language);
			gc(language);
			if (debug) {
				if (ids.size() > 0) {
					System.out.println("top: " + (String)language.data);
					System.out.println("ids: " + ids.size() + " " + ids.keySet());
//					System.out.println(toString(language));
				}
				System.out.println(s.charAt(i));
			}
		}
		if (debug) {
		System.out.println(s);
		System.out.println(toString(language));
		}
		result = nullable(language);
		ids.clear();
		derivations.clear();
		ids = startids;
		derivations = startderivations;
		return result;
	}
	public boolean matches(String s) {
		return matches(definition, s);
	}
}
