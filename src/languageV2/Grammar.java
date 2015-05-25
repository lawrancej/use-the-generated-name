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
	public static final TaggedData<Character> any = new TaggedData<Character>(Construct.SYMBOL.ordinal(), null);
	// Symbol cache
	private TaggedDataCache<Character> symbols = new TaggedDataCache<Character>(any);
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
	public static final TaggedData<LanguagePair> empty = new TaggedData<LanguagePair>(Construct.LIST.ordinal(), null);
	// List cache
	private TaggedDataCache<LanguagePair> lists = new TaggedDataCache<LanguagePair>(empty);
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
	public static final TaggedData<SetOfLanguages> reject = new TaggedData<SetOfLanguages>(Construct.SET.ordinal(),null);
	// Set cache
	private TaggedDataCache<SetOfLanguages> ors = new TaggedDataCache<SetOfLanguages>(reject);
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
	private TaggedDataCache<TaggedData<?>> stars = new TaggedDataCache<TaggedData<?>>(new TaggedData<TaggedData<?>>(Construct.LOOP.ordinal(), null));
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
	private Map<String, TaggedData<?>> derivations = new HashMap<String, TaggedData<?>>();
	public class Id extends TaggedData<String> {
		Id(String label) {
			super(Construct.ID.ordinal(), label);
		}
		public void derives(TaggedData<?>... languages) {
			derivations.put(data, or(rhs(data), list(languages)));
		}
	}
	// Identifier lookup by name
	private Map<String, Id> ids = new HashMap<String, Id>();
	private TaggedData<?> rhs(String s) {
		if (!derivations.containsKey(s)) {
			return reject;
		}
		return derivations.get(s);
	}

	private Set<TaggedData<?>> nulls = new HashSet<TaggedData<?>>();
	private Set<TaggedData<?>> terms = new HashSet<TaggedData<?>>();
	public boolean debug = false;
	/**
	 * Define the language
	 * @param language
	 */
	public void define(TaggedData<?> language) {
		definition = language;
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
	private boolean nullable(Set<String> visited, TaggedData<?> language) {
		boolean result = false;
		if (nulls.contains(language)) return true;
		switch(Construct.values()[language.tag]) {
		case ID:
			if (!visited.contains((String) language.data)) {
				visited.add((String) language.data);
				result = nullable(visited,rhs((String)language.data));
				if (result) {
					nulls.add(language);
				}
			}
/*			if (debug) {
				System.out.println("nullable: " + result);
				System.out.println(((Id)language).data.left);
				System.out.println(show(((Id)language).data.right.current));
			} */
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
	public boolean nullable(TaggedData<?> language) {
		return nullable(new HashSet<String>(), language);
	}
	public boolean nullable() {
		return nullable(definition);
	}
	// Is the identifier a terminal?
	private boolean terminal(Set<String> visited, TaggedData<?> language) {
		boolean result = true;
		if (terms.contains(language)) return true;
		switch(Construct.values()[language.tag]) {
		case SET:
			if (language.data != null) {
				for (TaggedData<?> l : ((TaggedData<SetOfLanguages>)language).data) {
					result = result && terminal(visited, l);
				}
			}
			break;
		case SYMBOL:
			break;
		case ID:
			if (!visited.contains(((Id) language).data)) {
				visited.add(((Id) language).data);
				result = terminal(visited, rhs((String)language.data));
				if (result) {
					terms.add(language);
				}
			}
			else {
				result = false;
			}
			break;
		case LIST:
			if (language.data != null) {
				result = terminal(visited, ((TaggedData<LanguagePair>)language).data.left) &&
						terminal(visited, ((TaggedData<LanguagePair>)language).data.right);
			}
			break;
		case LOOP:
			result = terminal(visited, ((TaggedData<TaggedData<?>>)language).data);
		}
		return result;
	}
	public boolean terminal(String s, TaggedData<?> language) {
		HashSet<String> identifiers = new HashSet<String>();
		identifiers.add(s);
		return terminal(identifiers, language);
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
		Set<String> visited = new HashSet<String>();
		for (int i = 0; i < s.length(); i++) {
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
		result = nullable(visited,language);
		return result;
	}
	public boolean matches(String s) {
		return matches(definition, s);
	}
}
