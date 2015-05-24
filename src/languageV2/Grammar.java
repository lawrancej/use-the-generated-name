package languageV2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Grammar {
	public static enum Construct {
		SYMBOL /* c */,
		LIST /* abc... */,
		SET /* a|b|c... */,
		LOOP /* a* */,
		ID, /* id -> derivation */
	};
	public static class Mutable<T> {
		public T current;
		Mutable(T data) {
			current = data;
		}
	}
	@SuppressWarnings("serial")
	public static class SetOfLanguages extends HashSet<TaggedData<?>> {}
	public static class LanguageSet extends TaggedData<SetOfLanguages> {
		public LanguageSet(SetOfLanguages data) {
			super(Construct.SET.ordinal(), data);
		}
	}
	public static class LanguagePair extends Pair<TaggedData<?>,TaggedData<?>> {
		public LanguagePair(TaggedData<?> left, TaggedData<?> right) {
			super(left, right);
		}
	}
	public static class Symbol extends TaggedData<Character> {
		Symbol(Character data) {
			super(Construct.SYMBOL.ordinal(), data);
		}
	}
	public static class BinaryOperator extends TaggedData<LanguagePair> {
		BinaryOperator(LanguagePair data) {
			super(Construct.LIST.ordinal(), data);
		}
	}
	public static class Loop extends TaggedData<TaggedData<?>> {
		Loop(TaggedData<?> data) {
			super(Construct.LOOP.ordinal(), data);
		}
	}
	public class Id extends TaggedData<Pair<String,Mutable<TaggedData<?>>>> {
		Id(String label) {
			super(Construct.ID.ordinal(), new Pair<String, Mutable<TaggedData<?>>>(label, new Mutable<TaggedData<?>>(reject)));
		}
		public void derives(TaggedData<?>... languages) {
			data.right.current = or(data.right.current, list(languages));
		}
	}
	/* Singletons */
	public static final Symbol any = new Symbol(null);
	public static final LanguageSet reject = new LanguageSet(null);
	public static final BinaryOperator empty = new BinaryOperator(null);
	/* Flyweights */
	/* Pattern: map what's inside the language to the language */
	private static Map<Character, Grammar.Symbol> symbols = new HashMap<Character, Grammar.Symbol>();
	private Map<TaggedData<?>, Loop> stars = new HashMap<TaggedData<?>, Loop>();
	private Map<SetOfLanguages, LanguageSet> ors = new HashMap<SetOfLanguages, LanguageSet>();
	private Map<LanguagePair, BinaryOperator> lists = new HashMap<LanguagePair, BinaryOperator>();
	private Map<String, Id> ids = new HashMap<String, Id>();
	private Set<TaggedData<?>> nulls = new HashSet<TaggedData<?>>();
	private Set<TaggedData<?>> terms = new HashSet<TaggedData<?>>();
	public boolean debug = false;
	public TaggedData<?> definition = reject;
	public Grammar() {
		
	}
	public static Symbol symbol(char c) {
		if (!symbols.containsKey(c)) {
			symbols.put(c, new Symbol(c));
		}
		return symbols.get(c);
	}
	public TaggedData<?> string(String s) {
		TaggedData<?>[] array = new TaggedData<?>[s.length()];
		for (int i = 0; i < s.length(); i++) {
			array[i] = symbol(s.charAt(i));
		}
		return list(array, 0);
	}
	public TaggedData<?> many(TaggedData<?> language) {
		if (Construct.values()[language.tag] == Construct.LOOP) return language;
		if (!stars.containsKey(language)) {
			stars.put(language, new Loop(language));
		}
		return stars.get(language);
	}
	private TaggedData<?> um(SetOfLanguages s) {
		if (!ors.containsKey(s)) {
			ors.put(s, new LanguageSet(s));
		}
		return ors.get(s);
	}
	private TaggedData<?> merge(LanguageSet set, TaggedData<?> item) {
		if (set.data.contains(item)) {
			return set;
		} else {
			SetOfLanguages s = (SetOfLanguages)set.data.clone();
			s.add(item);
			return um(s);
		}
	}
	private TaggedData<?> mergeAll(LanguageSet set, LanguageSet set2) {
		if (set.data.containsAll(set2.data)) {
			return set;
		} else {
			SetOfLanguages s = (SetOfLanguages)set.data.clone();
			s.addAll(set2.data);
			return um(s);
		}
	}
	private TaggedData<?> orInstance(TaggedData<?> left, TaggedData <?> right) {
		assert left != null;
		assert right != null;
		if (left == reject) { return right; }
		if (right == reject) { return left; }
		if (left == right) { return left; }
		SetOfLanguages setOfLanguages;
		// Do the types differ?
		if (left.tag != right.tag) {
			if (Construct.values()[left.tag] == Construct.SET) {
				return merge((LanguageSet)left, right);
			}
			else if (Construct.values()[right.tag] == Construct.SET) {
				return merge((LanguageSet)right, left);
			}
		}
		// The types are the same, and they're both sets
		else if (Construct.values()[left.tag] == Construct.SET) {
			return mergeAll((LanguageSet)left, (LanguageSet)right);
		}
		setOfLanguages = new SetOfLanguages();
		setOfLanguages.add(left);
		setOfLanguages.add(right);
		return um(setOfLanguages);
	}
	private TaggedData<?> or(TaggedData<?>[] nodes, int i) {
		if (i >= nodes.length) {
			return reject;
		} else {
			return orInstance(nodes[i], or(nodes, i+1));
		}
	}
	public TaggedData<?> or(TaggedData<?>... nodes) {
		return or(nodes, 0);
	}
	public TaggedData<?> option(TaggedData<?> language) {
		return or(language, empty);
	}
	private TaggedData<?> listInstance(TaggedData<?> left, TaggedData <?> right) {
		if (left == reject || right == reject) {
			return reject;
		}
		if (left == empty) { return right; }
		if (right == empty) { return left; }
		LanguagePair pair = new LanguagePair(left, right);
		if (!lists.containsKey(pair)) {
			lists.put(pair, new BinaryOperator(pair));
		}
		return lists.get(pair);
	}
	private TaggedData<?> list(TaggedData<?>[] nodes, int i) {
		if (i >= nodes.length) {
			return empty;
		} else {
			return listInstance(nodes[i], list(nodes, i+1));
		}
	}
	public TaggedData<?> list(TaggedData<?>... nodes) {
		return list(nodes, 0);
	}
	public Id id(String s) {
		Id result;
		if (!ids.containsKey(s)) {
			ids.put(s, new Id(s));
		}
		result = ids.get(s);
		if (definition == reject) {
			definition = result;
		}
		return result;
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
		
		if (terminal(rhs)) {
			return rhs;
		}
		*/
		id(s).derives(languages);
		return id(s);
	}
	private static StringBuffer show(StringBuffer buffer, TaggedData<?> language) {
		switch(Construct.values()[language.tag]) {
		case ID:
			buffer.append('<');
			buffer.append(((Id)language).data.left);
			buffer.append('>');
			break;
		case LIST:
			if (language.data == null) {
				buffer.append("\u03b5");
			} else {
				buffer.append('(');
				show(buffer,((BinaryOperator)language).data.left);
				buffer.append(' ');
				show(buffer,((BinaryOperator)language).data.right);
				buffer.append(')');
			}
			break;
		case SET:
			if (language.data == null) {
				buffer.append("\u2205");
			} else {
				buffer.append('(');
				boolean flag = false;
				for (TaggedData<?> l : ((LanguageSet)language).data) {
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
			show(buffer,((Loop)language).data);
			buffer.append(")*");
			break;
		case SYMBOL:
			buffer.append('\'');
			if (language.data == null) {
				buffer.append("<any character>");
			} else {
				buffer.append(((Grammar.Symbol)language).data);
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
				buffer.append(id.data.left);
				buffer.append('>');
				buffer.append(" ::= ");
				show(buffer,id.data.right.current);
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
	private boolean nullable(Set<Id> visited, TaggedData<?> language) {
		boolean result = false;
		if (nulls.contains(language)) return true;
		switch(Construct.values()[language.tag]) {
		case ID:
			if (!visited.contains((Id) language)) {
				visited.add((Id) language);
				result = nullable(visited,((Id)language).data.right.current);
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
				for (TaggedData<?> l : ((LanguageSet)language).data) {
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
				result = nullable(visited, ((BinaryOperator)language).data.left) &&
						nullable(visited, ((BinaryOperator)language).data.right);
			} else {
				result = true;
			}
		}
		return result;
	}
	public boolean nullable(TaggedData<?> language) {
		return nullable(new HashSet<Id>(), language);
	}
	public boolean nullable() {
		return nullable(definition);
	}
	// Is the identifier a terminal?
	private boolean terminal(Set<Id> visited, TaggedData<?> language) {
		boolean result = true;
		if (terms.contains(language)) return true;
		switch(Construct.values()[language.tag]) {
		case SET:
			if (language.data != null) {
				for (TaggedData<?> l : ((LanguageSet)language).data) {
					result = result && terminal(visited, l);
				}
			}
			break;
		case SYMBOL:
			break;
		case ID:
			if (!visited.contains((Id) language)) {
				visited.add((Id) language);
				result = terminal(visited,((Id)language).data.right.current);
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
				result = terminal(visited, ((BinaryOperator)language).data.left) &&
						terminal(visited, ((BinaryOperator)language).data.right);
			}
			break;
		case LOOP:
			result = terminal(visited, ((Loop)language).data);
		}
		return result;
	}
	public boolean terminal(TaggedData<?> language) {
		return terminal(new HashSet<Id>(), language);
	}
	public boolean terminal() {
		return terminal(definition);
	}

	private TaggedData<?> derivative(Set<Id> visited, char c, TaggedData<?> language) {
		switch(Construct.values()[language.tag]) {
		case ID:
			Id id = (Id)language;
			String dc = "D" + c + id.data.left;
			if (!visited.contains(id)) {
				visited.add(id);
				return derives(dc, derivative(visited, c, id.data.right.current));
			} else if (ids.containsKey(dc)) {
				return id(dc);
			}
			break;
		case LIST:
			if (language.data != null) {
				TaggedData<?> result = list(derivative(visited, c, ((BinaryOperator)language).data.left),
						((BinaryOperator)language).data.right);
				if (nullable(((BinaryOperator)language).data.left)) {
					return or(result, derivative(visited, c, ((BinaryOperator)language).data.right));
				}
				return result;
			}
			break;
		case SET:
			if (language.data != null) {
				TaggedData<?> result = reject;
				for (TaggedData<?> l : ((LanguageSet)language).data) {
					result = or(result, derivative(visited, c, l));
				}
				return result;
			}
			break;
		case LOOP:
			return list(derivative(visited, c, ((Loop)language).data), language);
		case SYMBOL:
			if (language.data == null || ((Symbol)language).data == c) {
				return empty;
			}
		}
		return reject;
	}
	public TaggedData<?> derivative(char c, TaggedData<?> language) {
		return derivative(new HashSet<Id>(), c, language);
	}
	public TaggedData<?> derivative(char c) {
		return derivative(c, definition);
	}

	private TaggedData<?> first(HashSet<Id> visited, TaggedData<?> language) {
		switch(Construct.values()[language.tag]) {
		case ID:
			Id id = (Id) language;
			if (!visited.contains(id)) {
				visited.add(id);
				return first(visited, id.data.right.current);
			}
			break;
		case LIST:
			if (language.data != null) {
				TaggedData<?> result = first(visited, ((BinaryOperator)language).data.left);
				if (nullable(((BinaryOperator)language).data.left)) {
					result = or(result, first(visited, ((BinaryOperator)language).data.right));
				}
				return result;
			}
			break;
		case SET:
			if (language.data != null) {
				TaggedData<?> result = reject;
				for (TaggedData<?> l : ((LanguageSet)language).data) {
					result = or(result, first(visited, l));
				}
				return result;
			}
			break;
		case LOOP:
			return first(visited, ((Loop)language).data);
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
		if (debug) {
			System.out.println(show(language));
		}
		Set<Id> visited = new HashSet<Id>();
		for (int i = 0; i < s.length(); i++) {
			language = derivative(visited, s.charAt(i), language);
			visited.clear();
			if (debug) {
				System.out.println(s.charAt(i));
				System.out.println(show(language));
			}
		}
		return nullable(visited,language);
	}
	public boolean matches(String s) {
		return matches(definition, s);
	}
}
