package languageV2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Grammar {
	public static enum Construct {
		SYMBOL /* c */,
		LIST /* abc... */,
		SET /* a|b|c... */,
		LOOP /* a* */,
		ID, /* id -> derivation */
	};
	public static class Language<T> {
		public final Construct type;
		public final T data;
		Language(Construct type, T data) {
			this.type = type;
			this.data = data;
		}
	}
	public static class Mutable<T> {
		public T current;
		Mutable(T data) {
			current = data;
		}
	}
	@SuppressWarnings("serial")
	public static class SetOfLanguages extends HashSet<Language<?>> {}
	public static class LanguageSet extends Language<SetOfLanguages> {
		public LanguageSet(Construct type, SetOfLanguages data) {
			super(type, data);
		}
	}
	public static class LanguagePair extends Pair<Language<?>,Language<?>> {
		public LanguagePair(Language<?> left, Language<?> right) {
			super(left, right);
		}
	}
	public static class Symbol extends Language<Character> {
		Symbol(Construct type, Character data) {
			super(type, data);
		}
	}
	public static class BinaryOperator extends Language<LanguagePair> {
		BinaryOperator(Construct type, LanguagePair data) {
			super(type, data);
		}
	}
	public static class Loop extends Language<Language<?>> {
		Loop(Construct type, Language<?> data) {
			super(type, data);
		}
	}
	public class Id extends Language<Pair<String,Mutable<Language<?>>>> {
		Id(String label) {
			super(Construct.ID, new Pair<String, Mutable<Language<?>>>(label, new Mutable<Language<?>>(reject)));
		}
		public void derives(Language<?>... languages) {
			data.right.current = or(data.right.current, list(languages));
		}
	}
	/* Singletons */
	public static final Symbol any = new Symbol(Construct.SYMBOL, null);
	public static final LanguageSet reject = new LanguageSet(Construct.SET, null);
	public static final Language<Void> empty = new Language<Void>(Construct.LIST, null);
	/* Flyweights */
	/* Pattern: map what's inside the language to the language */
	private static Map<Character, Grammar.Symbol> symbols = new HashMap<Character, Grammar.Symbol>();
	private Map<Language<?>, Loop> stars = new HashMap<Language<?>, Loop>();
	private Map<SetOfLanguages, LanguageSet> ors = new HashMap<SetOfLanguages, LanguageSet>();
	private Map<LanguagePair, BinaryOperator> lists = new HashMap<LanguagePair, BinaryOperator>();
	private Map<String, Id> ids = new HashMap<String, Id>();
	private Set<Language<?>> nulls = new HashSet<Language<?>>();
	private Set<Language<?>> terms = new HashSet<Language<?>>();
	public boolean debug = false;
	public static Symbol symbol(char c) {
		if (!symbols.containsKey(c)) {
			symbols.put(c, new Grammar.Symbol(Construct.SYMBOL, c));
		}
		return symbols.get(c);
	}
	public Language<?> string(String s) {
		Language<?>[] array = new Language<?>[s.length()];
		for (int i = 0; i < s.length(); i++) {
			array[i] = symbol(s.charAt(i));
		}
		return list(array, 0);
	}
	public Language<?> many(Language<?> language) {
		if (language.type == Construct.LOOP) return language;
		if (!stars.containsKey(language)) {
			stars.put(language, new Loop(Construct.LOOP, language));
		}
		return stars.get(language);
	}
	private Language<?> um(SetOfLanguages s) {
		if (!ors.containsKey(s)) {
			ors.put(s, new LanguageSet(Construct.SET, s));
		}
		return ors.get(s);
	}
	private Language<?> merge(LanguageSet set, Language<?> item) {
		if (set.data.contains(item)) {
			return set;
		} else {
			SetOfLanguages s = (SetOfLanguages)set.data.clone();
			s.add(item);
			return um(s);
		}
	}
	private Language<?> mergeAll(LanguageSet set, LanguageSet set2) {
		if (set.data.containsAll(set2.data)) {
			return set;
		} else {
			SetOfLanguages s = (SetOfLanguages)set.data.clone();
			s.addAll(set2.data);
			return um(s);
		}
	}
	private Language<?> orInstance(Language<?> left, Language <?> right) {
		assert left != null;
		assert right != null;
		if (left == reject) { return right; }
		if (right == reject) { return left; }
		if (left == right) { return left; }
		SetOfLanguages setOfLanguages;
		// Do the types differ?
		if (left.type != right.type) {
			if (left.type == Construct.SET) {
				return merge((LanguageSet)left, right);
			}
			else if (right.type == Construct.SET) {
				return merge((LanguageSet)right, left);
			}
		}
		// The types are the same, and they're both sets
		else if (left.type == Construct.SET) {
			return mergeAll((LanguageSet)left, (LanguageSet)right);
		}
		setOfLanguages = new SetOfLanguages();
		setOfLanguages.add(left);
		setOfLanguages.add(right);
		return um(setOfLanguages);
	}
	private Language<?> or(Language<?>[] nodes, int i) {
		if (i >= nodes.length) {
			return reject;
		} else {
			return orInstance(nodes[i], or(nodes, i+1));
		}
	}
	public Language<?> or(Language<?>... nodes) {
		return or(nodes, 0);
	}
	public Language<?> option(Language<?> language) {
		return or(language, empty);
	}
	private Language<?> listInstance(Language<?> left, Language <?> right) {
		if (left == reject || right == reject) {
			return reject;
		}
		if (left == empty) { return right; }
		if (right == empty) { return left; }
		LanguagePair pair = new LanguagePair(left, right);
		if (!lists.containsKey(pair)) {
			lists.put(pair, new BinaryOperator(Construct.LIST, pair));
		}
		return lists.get(pair);
	}
	private Language<?> list(Language<?>[] nodes, int i) {
		if (i >= nodes.length) {
			return empty;
		} else {
			return listInstance(nodes[i], list(nodes, i+1));
		}
	}
	public Language<?> list(Language<?>... nodes) {
		return list(nodes, 0);
	}
	public Id id(String s) {
		if (!ids.containsKey(s)) {
			ids.put(s, new Id(s));
		}
		return ids.get(s);
	}
	private Language<?> derives(String s, Language<?>... languages) {
		Language<?> rhs = list(languages);
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
	private static StringBuffer show(StringBuffer buffer, Language<?> language) {
		switch(language.type) {
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
				for (Language<?> l : ((LanguageSet)language).data) {
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
	public String show(Language<?> language) {
		StringBuffer buffer = new StringBuffer();
		if (language.type == Construct.ID) {
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
		return show(language());
	}
	// Does the language derive the empty string?
	private boolean nullable(Set<Id> visited, Language<?> language) {
		boolean result = false;
		if (nulls.contains(language)) return true;
		switch(language.type) {
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
				for (Language<?> l : ((LanguageSet)language).data) {
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
	public boolean nullable(Language<?> language) {
		return nullable(new HashSet<Id>(), language);
	}
	public boolean nullable() {
		return nullable(language());
	}
	// Is the identifier a terminal?
	private boolean terminal(Set<Id> visited, Language<?> language) {
		boolean result = true;
		if (terms.contains(language)) return true;
		switch(language.type) {
		case SET:
			if (language.data != null) {
				for (Language<?> l : ((LanguageSet)language).data) {
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
	public boolean terminal(Language<?> language) {
		return terminal(new HashSet<Id>(), language);
	}
	public boolean terminal() {
		return terminal(language());
	}

	private Language<?> derivative(Set<Id> visited, char c, Language<?> language) {
		switch(language.type) {
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
				Language<?> result = list(derivative(visited, c, ((BinaryOperator)language).data.left),
						((BinaryOperator)language).data.right);
				if (nullable(((BinaryOperator)language).data.left)) {
					return or(result, derivative(visited, c, ((BinaryOperator)language).data.right));
				}
				return result;
			}
			break;
		case SET:
			if (language.data != null) {
				Language<?> result = reject;
				for (Language<?> l : ((LanguageSet)language).data) {
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
	public Language<?> derivative(char c, Language<?> language) {
		return derivative(new HashSet<Id>(), c, language);
	}
	public Language<?> derivative(char c) {
		return derivative(c, language());
	}

	private Language<?> first(HashSet<Id> visited, Language<?> language) {
		switch(language.type) {
		case ID:
			Id id = (Id) language;
			if (!visited.contains(id)) {
				visited.add(id);
				return first(visited, id.data.right.current);
			}
			break;
		case LIST:
			if (language.data != null) {
				Language<?> result = first(visited, ((BinaryOperator)language).data.left);
				if (nullable(((BinaryOperator)language).data.left)) {
					result = or(result, first(visited, ((BinaryOperator)language).data.right));
				}
				return result;
			}
			break;
		case SET:
			if (language.data != null) {
				Language<?> result = reject;
				for (Language<?> l : ((LanguageSet)language).data) {
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
	public Language<?> first(Language<?> language) {
		return first(new HashSet<Id>(), language);
	}
	public Language<?> first() {
		return first(language());
	}
	public boolean matches(Language<?> language, String s) {
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
		return matches(language(), s);
	}

	public abstract Language<?> language();
}
