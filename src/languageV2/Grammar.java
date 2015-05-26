package languageV2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class Grammar {
	public static enum Construct {
		SYMBOL /* c */,
		LIST /* abc... */,
		SET /* a|b|c... */,
		LOOP /* a* */,
		ID, /* id -> derivation */
	};
	@SuppressWarnings("serial")
	public static class SetOfLanguages extends HashSet<TaggedData<?>> {}
	public static class LanguagePair extends Pair<TaggedData<?>,TaggedData<?>> {
		public LanguagePair(TaggedData<?> left, TaggedData<?> right) {
			super(left, right);
		}
	}
	/** Symbols */
	/** Match any symbol */
	public static final TaggedData<Character> any = TaggedData.create(Construct.SYMBOL.ordinal(), null);
	// Symbol cache
	private TaggedDataCache<Character> symbols = TaggedDataCache.create(any);
	/**
	 * Matches a character
	 * @param c The character to match
	 * @return The language
	 */
	public TaggedData<?> symbol(char c) {
		return symbols.getInstance(c);
	}
	/** Lists */
	// Empty list (string)
	public static final TaggedData<LanguagePair> empty = TaggedData.create(Construct.LIST.ordinal(), null);
	// List cache
	private TaggedDataCache<LanguagePair> lists = TaggedDataCache.create(empty);
	// Get a list from the cache
	private TaggedData<?> listInstance(TaggedData<?> left, TaggedData <?> right) {
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
	 * Match a list of languages
	 * @param nodes A list of languages
	 * @return A language matching the list of languages, in order
	 */
	public TaggedData<?> list(TaggedData<?>... nodes) {
		return list(nodes, 0);
	}
	/** Sets */
	// Empty set (reject)
	public static final TaggedData<SetOfLanguages> reject = TaggedData.create(Construct.SET.ordinal(),null);
	// Set cache
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
		// The types are the same, and they're both sets
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
	 * Match any of the languages
	 * @param nodes Languages
	 * @return A language matching any of the languages
	 */
	public TaggedData<?> or(TaggedData<?>... nodes) {
		return or(nodes, 0);
	}
	/** Loops (Kleene stars) */
	// Loop cache
	private TaggedDataCache<TaggedData<?>> stars = TaggedDataCache.create(new TaggedData<TaggedData<?>>(Construct.LOOP.ordinal(), null));
	/**
	 * Match a language zero or more times
	 * @param language The language
	 * @return A language that matches the input language zero or more times
	 */
	public TaggedData<?> many(TaggedData<?> language) {
		assert language != null;
		if (Construct.values()[language.tag] == Construct.LOOP) return language;
		return stars.getInstance(language);
	}
	/** Identifiers (Terminals and nonterminals) */
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
	private TaggedData<?> rhs(String s) {
		if (!derivations.containsKey(s)) {
			return reject;
		}
		return derivations.get(s);
	}
	// Set of terminal identifiers
	private Set<String> terms = new HashSet<String>();
	// Set of nonterminal identifiers
	private Set<String> nonterms = new HashSet<String>();
	// Set of identifiers deriving empty
	private Set<String> nulls = new HashSet<String>();
	public boolean debug = false;
	/**
	 * Define the language
	 * @param language
	 */
	public void define(TaggedData<?> language) {
		definition = language;
		// Classify identifiers
		classify();
		// Analyze nullability
		nullable();
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
	private TaggedData<?> definition = reject;
	public Grammar() {}
	public TaggedData<?> string(String s) {
		TaggedData<?>[] array = new TaggedData<?>[s.length()];
		for (int i = 0; i < s.length(); i++) {
			array[i] = symbol(s.charAt(i));
		}
		return list(array, 0);
	}
	public TaggedData<?> option(TaggedData<?> language) {
		return or(language, empty);
	}
	public Id id(String s) {
		if (!ids.containsKey(s)) {
			ids.put(s, new Id(s));
		}
		return ids.get(s);
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
	// Show a language
	private static StringBuffer show(StringBuffer buffer, TaggedData<?> language) {
		switch(Construct.values()[language.tag]) {
		case ID:
			buffer.append('<');
			buffer.append(((Id)language).data);
			buffer.append('>');
			break;
		case LIST:
			if (language.data == null) {
				buffer.append("\u03b5");
			} else {
				buffer.append('(');
				show(buffer,((TaggedData<LanguagePair>)language).data.left);
				buffer.append(' ');
				show(buffer,((TaggedData<LanguagePair>)language).data.right);
				buffer.append(')');
			}
			break;
		case SET:
			if (language.data == null) {
				buffer.append("\u2205");
			} else {
				buffer.append('(');
				boolean flag = false;
				for (TaggedData<?> l : ((TaggedData<SetOfLanguages>)language).data) {
					if (flag) {
						buffer.append('|');
					} else {
						flag = true;
					}
					show(buffer, l);
				}
				buffer.append(')');
			}
			break;
		case LOOP:
			buffer.append('(');
			show(buffer,((TaggedData<TaggedData<?>>)language).data);
			buffer.append(")*");
			break;
		case SYMBOL:
			buffer.append('\'');
			if (language.data == null) {
				buffer.append("<any character>");
			} else {
				buffer.append(((TaggedData<Character>)language).data);
			}
			buffer.append('\'');
			break;
		default:
			break;
		}
		return buffer;
	}
	public String show(TaggedData<?> language) {
		StringBuffer buffer = new StringBuffer();
		if (Construct.values()[language.tag] == Construct.ID) {
			for (Id id : ids.values()) {
				buffer.append('<');
				buffer.append(id.data);
				buffer.append('>');
				buffer.append(" ::= ");
				show(buffer,rhs(id.data));
				buffer.append("\n");
			}
			return buffer.toString();
		} else {
			return show(buffer, language).toString();
		}
	}
	public String toString() {
		return show(definition);
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
		}
		return result;
	}
	private boolean nullable(TaggedData<?> language) {
		return nullable(new HashSet<TaggedData<?>>(), language);
	}
	public boolean nullable() {
		return nullable(definition);
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
				TaggedData<?> result = list(derivative(visited, c, ((TaggedData<LanguagePair>)language).data.left),
						((TaggedData<LanguagePair>)language).data.right);
				if (nullable(((TaggedData<LanguagePair>)language).data.left)) {
					return or(result, derivative(visited, c, ((TaggedData<LanguagePair>)language).data.right));
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
		}
		return reject;
	}
	public TaggedData<?> derivative(char c, TaggedData<?> language) {
		return derivative(new HashSet<String>(), c, language);
	}
	public TaggedData<?> derivative(char c) {
		return derivative(c, definition);
	}

	// Compute the first set
	private TaggedData<?> first(HashSet<Id> visited, TaggedData<?> language) {
		switch(Construct.values()[language.tag]) {
		case ID:
			Id id = (Id) language;
			if (!visited.contains(id)) {
				visited.add(id);
				return first(visited, rhs(id.data));
			}
			break;
		case LIST:
			if (language.data != null) {
				TaggedData<?> result = first(visited, ((TaggedData<LanguagePair>)language).data.left);
				if (nullable(((TaggedData<LanguagePair>)language).data.left)) {
					result = or(result, first(visited, ((TaggedData<LanguagePair>)language).data.right));
				}
				return result;
			}
			break;
		case SET:
			if (language.data != null) {
				TaggedData<?> result = reject;
				for (TaggedData<?> l : ((TaggedData<SetOfLanguages>)language).data) {
					result = or(result, first(visited, l));
				}
				return result;
			}
			break;
		case LOOP:
			return first(visited, ((TaggedData<TaggedData<?>>)language).data);
		case SYMBOL:
			return language;
		}
		return reject;
	}
	public TaggedData<?> first(TaggedData<?> language) {
		return first(new HashSet<Id>(), language);
	}
	public TaggedData<?> first() {
		return first(definition);
	}
	public boolean matches(TaggedData<?> language, String s) {
		boolean result;
		if (debug) {
			System.out.println(show(language));
		}
		Map<String, Id> startids = new HashMap<String, Id>();
		Map<String, TaggedData<?>> startderivations = new HashMap<String, TaggedData<?>>();
		for (Id id : ids.values()) {
			startids.put(id.data, id);
			startderivations.put(id.data, rhs(id.data));
		}
		Set<String> visited = new HashSet<String>();
		for (int i = 0; i < s.length(); i++) {
/*			if (language.tag == Construct.ID.ordinal()) {
				visited.add((String)language.data);
			}
*/
			language = derivative(visited, s.charAt(i), language);
			if (!visited.isEmpty()) {
				System.out.println("top: " + (String)language.data);
				System.out.println("visited: " + visited.size() + " "+ visited);
				System.out.println("ids: " + ids.size() + " " + ids.keySet());
			}
			visited.clear();
			if (debug) {
				System.out.println(s.charAt(i));
				System.out.println(show(language));
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
