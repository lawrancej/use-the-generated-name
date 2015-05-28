package languageV2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class Grammar {
	/** A language is a set of lists of symbols.
	 * Loops define repetition, and identifiers enable recursion. */
	public static enum Construct {
		SYMBOL /* c */,
		LIST /* abc... */,
		SET /* a|b|c... */,
		LOOP /* a* */,
		ID, /* id */
	};
	/** Symbols match a character. The null symbol matches any character. */
	public static final TaggedData<Character> any = TaggedData.create(Construct.SYMBOL.ordinal(), null);
	private TaggedDataCache<Character> symbols = TaggedDataCache.create(any);
	/**
	 * Match a character
	 * @param c The character
	 * @return A language matching character c.
	 */
	public TaggedData<?> symbol(char c) {
		return symbols.getInstance(c);
	}
	/** Lists match a sequence. The null list matches the empty sequence. */
	public static final TaggedData<LanguagePair> empty = TaggedData.create(Construct.LIST.ordinal(), null);
	private TaggedDataCache<LanguagePair> lists = TaggedDataCache.create(empty);
	// Get a list from the cache
	private TaggedData<?> listInstance(TaggedData<?> left, TaggedData <?> right) {
		// Avoid creating a new list, if possible
		if (left == reject || right == reject) {
			return reject;
		}
		if (left == empty) { return right; }
		if (right == empty) { return left; }
		LanguagePair pair = new LanguagePair(left, right);
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
	 * @param nodes A sequence of languages
	 * @return A language matching the languages in order
	 */
	public TaggedData<?> list(TaggedData<?>... nodes) {
		return list(nodes, 0);
	}
	/**
	 * Match String s literally.
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
	private Map<String, Id> ids = new HashMap<String, Id>();
	// Derivation (rhs) lookup by name
	private Map<String, TaggedData<?>> derivations = new HashMap<String, TaggedData<?>>();
	public class Id extends TaggedData<String> {
		Id(String label) {
			super(Construct.ID.ordinal(), label);
		}
		public void derives(TaggedData<?>... languages) {
			derivations.put(data, or(rhs(data), list(languages)));
		}
	}
	/**
	 * Declare or use an identifier (terminal or nonterminal).
	 * @param s The identifier name.
	 * @return The identifier.
	 */
	public Id id(String s) {
		if (!ids.containsKey(s)) {
			ids.put(s, new Id(s));
		}
		return ids.get(s);
	}
	// Get the right hand side
	private TaggedData<?> rhs(String s) {
		if (!derivations.containsKey(s)) {
			return reject;
		}
		return derivations.get(s);
	}
	/** Visitors traverse a tree. */
	/**
	 * Visit a rule of the form id ::= rhs.
	 * Use this method to control traversal.
	 * @param visitor
	 * @param id
	 * @return
	 */
	public <T> T visit(Visitor<T> visitor, String id) {
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
			return visitor.id((String)language.data);
		case LIST:
			return visitor.list((LanguagePair)language.data);
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
	 * Visit the language definition.
	 * Use this method to begin traversal.
	 * @param visitor
	 * @return
	 */
	public <T> T visit(Visitor<T> visitor) {
		// Visit a grammar
		if (definition.tag == Construct.ID.ordinal()) {
			WorkList<String> rules = new WorkList<String>();
			rules.todo((String)definition.data);
			return visitor.top(this, rules);
		}
		// Visit a regex
		else {
			return visit(visitor, definition);
		}
	}
	/** The language definition. The root of all traversal. */
	private TaggedData<?> definition = reject;
	/**
	 * Define the language.
	 * For regular expressions, surround the definition with define().
	 * For grammars, call define() *after* specifying the language.
	 * @param language The language
	 */
	public void define(TaggedData<?> language) {
		definition = language;
		// Classify identifiers
//		classify();
		// Analyze nullability
//		nullable();
	}
	public Grammar() {}
	public boolean debug = false;
	@Override
	public String toString() {
		return visit(new Printer()).toString();
	}
	
	
	// Set of terminal identifiers
	private Set<String> terms = new HashSet<String>();
	// Set of nonterminal identifiers
	private Set<String> nonterms = new HashSet<String>();
	// Set of identifiers deriving empty
	private Set<String> nulls = new HashSet<String>();
	public Set<String> identifierSet() {
		return ids.keySet();
	}
	public Set<String> nullableSet() {
		return nulls;
	}
	public Set<String> nonterminalSet() {
		return nonterms;
	}
	public Set<String> terminalSet() {
		return terms;
	}
	// Classify identifiers into terminal and nonterminal sets
	private void classify(Set<String> visited, String s, TaggedData<?> language) {
		if (nonterms.contains(s)) return;
		if (terms.contains(s)) return;
		switch(Construct.values()[language.tag]) {
		case SET:
			if (language.data != null) {
				for (TaggedData<?> l : ((TaggedData<SetOfLanguages>)language).data) {
					classify(visited, s, l);
				}
			}
			return;
		case SYMBOL: return;
		case ID:
			String label = (String)language.data;
			if (!visited.contains(label)) {
				visited.add(label);
				classify(visited, s, rhs(label));
				if (!nonterms.contains(s)) {
					terms.add(s);
				}
			}
			else if (!terms.contains(s)) {
				nonterms.add(s);
			}
			return;
		case LIST:
			if (language.data != null) {
				classify(visited, s, ((TaggedData<LanguagePair>)language).data.left);
				classify(visited, s, ((TaggedData<LanguagePair>)language).data.right);
			}
			return;
		case LOOP:
			classify(visited, s, ((TaggedData<TaggedData<?>>)language).data);
			break;
		default:
			break;
		}
	}
	// Classify identifiers into: terminal, nonterminal
	private void classify() {
		HashSet<String> visited = new HashSet<String>();
		for (Id id : ids.values()) {
			classify(visited, id.data, rhs(id.data));
			visited.clear();
		}
	}
	private TaggedData<?> derives(String s, TaggedData<?>... languages) {
		TaggedData<?> rhs = list(languages);
		if (rhs == reject && !ids.containsKey(s)) {
			return reject;
		}
		/*
		if (rhs == empty && !ids.containsKey(s)) {
			return empty;
		}
		
		System.out.print(s);
		System.out.print(" -> ");
		System.out.println(show(rhs));
		if (terminal(s, rhs)) {
//			return rhs;
		}
		*/
		id(s).derives(languages);
		return id(s);
	}
	// Does the language derive the empty string?
	private boolean nullable(Set<TaggedData<?>> visited, TaggedData<?> language) {
		boolean result = false;
		switch(Construct.values()[language.tag]) {
		case ID:
			String label = (String) language.data;
			if (nulls.contains(label)) return true;
			if (!visited.contains(language)) {
				visited.add(language);
				result = nullable(visited,rhs(label));
				if (result) {
					nulls.add(label);
				}
			}
			break;
		case SET:
			if (language.data != null) {
				for (TaggedData<?> l : ((TaggedData<SetOfLanguages>)language).data) {
					result = result || nullable(visited, l);
				}
			}
			break;
		case SYMBOL:
			break;
		case LOOP:
			result = true;
			break;
		case LIST:
			if (language.data != null) {
				result = nullable(visited, ((TaggedData<LanguagePair>)language).data.left) &&
						nullable(visited, ((TaggedData<LanguagePair>)language).data.right);
			} else {
				result = true;
			}
			break;
		default:
			break;
		}
		return result;
	}
	public boolean nullable(TaggedData<?> language) {
		return nullable(new HashSet<TaggedData<?>>(), language);
	}
	public boolean nullable() {
		return nullable(definition);
	}
	public void todo(WorkList<String> list, TaggedData<?> language) {
		switch(Construct.values()[language.tag]) {
		case ID:
			list.todo((String)language.data);
			break;
		case LIST:
			if (language.data != null) {
				todo(list, ((TaggedData<LanguagePair>)language).data.left);
				todo(list, ((TaggedData<LanguagePair>)language).data.right);
			}
			break;
		case SET:
			if (language.data != null) {
				for (TaggedData<?> l : ((TaggedData<SetOfLanguages>)language).data) {
					todo(list, l);
				}
			}
			break;
		case LOOP:
			todo(list, ((TaggedData<TaggedData<?>>)language).data);
			break;
		case SYMBOL:
			break;
		default:
			break;
		}
	}
	// Compute the derivative of a language
	private TaggedData<?> derivative(Set<String> visited, char c, TaggedData<?> language) {
		switch(Construct.values()[language.tag]) {
		case ID:
			Id id = (Id)language;
			String dc = "D" + c + id.data;
			if (!visited.contains(id.data)) {
				visited.add(id.data);
				return derives(dc, derivative(visited, c, rhs(id.data)));
			} else if (ids.containsKey(dc)) {
				return id(dc);
			}
			break;
		case LIST:
			if (language.data != null) {
				TaggedData<LanguagePair> l = (TaggedData<LanguagePair>)language;
				TaggedData<?> result = list(derivative(visited, c, l.data.left), l.data.right);
				if (nullable(l.data.left)) {
					return or(result, derivative(visited, c, l.data.right));
				}
				return result;
			}
			break;
		case SET:
			if (language.data != null) {
				TaggedData<?> result = reject;
				for (TaggedData<?> l : ((TaggedData<SetOfLanguages>)language).data) {
					result = or(result, derivative(visited, c, l));
				}
				return result;
			}
			break;
		case LOOP:
			return list(derivative(visited, c, ((TaggedData<TaggedData<?>>)language).data), language);
		case SYMBOL:
			if (language.data == null || ((TaggedData<Character>)language).data == c) {
				return empty;
			}
			break;
		default:
			break;
		}
		return reject;
	}
	public TaggedData<?> derivative(char c, TaggedData<?> language) {
		return derivative(new HashSet<String>(), c, language);
	}
	public TaggedData<?> derivative(char c) {
		return derivative(c, definition);
	}

	public TaggedData<?> first() {
		return null; //first(definition);
	}
	public boolean matches(TaggedData<?> language, String s) {
		boolean result;
		if (debug) {
//			System.out.println(show(language));
		}
		Map<String, Id> startids = new HashMap<String, Id>();
		Map<String, TaggedData<?>> startderivations = new HashMap<String, TaggedData<?>>();
		for (Id id : ids.values()) {
			startids.put(id.data, id);
			startderivations.put(id.data, rhs(id.data));
		}
		Set<String> visited = new HashSet<String>();
		for (int i = 0; i < s.length(); i++) {
			language = derivative(visited, s.charAt(i), language);
			if (!visited.isEmpty()) {
				System.out.println("top: " + (String)language.data);
				System.out.println("visited: " + visited.size() + " "+ visited);
				System.out.println("ids: " + ids.size() + " " + ids.keySet());
			}
			visited.clear();
			nulls.clear();
			if (debug) {
				System.out.println(s.charAt(i));
//				System.out.println(show(language));
			}
		}
		result = nullable(language);
/*		ids.clear();
		derivations.clear();
		ids = startids;
		derivations = startderivations;
*/		return result;
	}
	public boolean matches(String s) {
		return matches(definition, s);
	}
}
