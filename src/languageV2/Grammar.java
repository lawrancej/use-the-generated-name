package languageV2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Or is still an issue: need to do proper union

public abstract class Grammar {
	public static enum Construct {
		REJECT /* empty set */, EMPTY /* empty string */,
		ANY /* . */, SYMBOL /* c */,
		LOOP /* a* */,
		OR /* a|b */, LIST /* ab */,
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
	public static final Language<Void> any = new Language<Void>(Construct.ANY, null);
	public static Language<Void> reject = new Language<Void>(Construct.REJECT, null);
	public static Language<Void> empty = new Language<Void>(Construct.EMPTY, null);
	/* Flyweights */
	/* Pattern: map what's inside the language to the language */
	private static Map<Character, Grammar.Symbol> symbols = new HashMap<Character, Grammar.Symbol>();
	private Map<Language<?>, Loop> stars = new HashMap<Language<?>, Loop>();
	private Map<LanguagePair, BinaryOperator> ors = new HashMap<LanguagePair, BinaryOperator>();
	private Map<LanguagePair, BinaryOperator> lists = new HashMap<LanguagePair, BinaryOperator>();
	private Map<String, Id> ids = new HashMap<String, Id>();
	public Language<Character> symbol(char c) {
		if (!symbols.containsKey(c)) {
			symbols.put(c, new Grammar.Symbol(Construct.SYMBOL, c));
		}
		return symbols.get(c);
	}
	public Language<?> many(Language<?> language) {
		if (language.type == Construct.LOOP) return language;
		if (!stars.containsKey(language)) {
			stars.put(language, new Loop(Construct.LOOP, language));
		}
		return stars.get(language);
	}
	private Language<?> orInstance(Language<?> left, Language <?> right) {
		if (left == reject) { return right; }
		if (right == reject) { return left; }
		if (left == right) { return left; }
		if (left.type == Construct.OR) {
			if (right == ((BinaryOperator)left).data.left) { return left; }
			if (right == ((BinaryOperator)left).data.right) { return left; }
		}
		if (right.type == Construct.OR) {
			if (left == ((BinaryOperator)right).data.left) { return right; }
			if (left == ((BinaryOperator)right).data.right) { return right; }
		}
		LanguagePair pair = new LanguagePair(left, right);
		if (!ors.containsKey(pair)) {
			ors.put(pair, new BinaryOperator(Construct.OR, pair));
		}
		return ors.get(pair);
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
	public void debug(String s, Language<?> lang) {
		System.out.print(s);
		System.out.println(s.hashCode());
		System.out.println(lang.type.name());
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
		if (rhs == empty && !ids.containsKey(s)) {
			return empty;
		}
		if (terminal(rhs)) {
			return rhs;
		}
		id(s).derives(languages);
		return id(s);
	}
	private static StringBuffer show(StringBuffer buffer, Language<?> language) {
		switch(language.type) {
		case ANY:
			buffer.append("<any character>");
			break;
		case EMPTY:
			buffer.append("\u03b5");
			break;
		case ID:
			buffer.append('<');
			buffer.append(((Id)language).data.left);
			buffer.append('>');
			break;
		case LIST:
			buffer.append('(');
			show(buffer,((BinaryOperator)language).data.left);
			buffer.append(' ');
			show(buffer,((BinaryOperator)language).data.right);
			buffer.append(')');
			break;
		case OR:
			buffer.append('(');
			show(buffer,((BinaryOperator)language).data.left);
			buffer.append('|');
			show(buffer,((BinaryOperator)language).data.right);
			buffer.append(')');
			break;
		case REJECT:
			buffer.append("\u2205");
			break;
		case LOOP:
			buffer.append('(');
			show(buffer,((Loop)language).data);
			buffer.append(")*");
			break;
		case SYMBOL:
			buffer.append('\'');
			buffer.append(((Grammar.Symbol)language).data);
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
		switch(language.type) {
		case EMPTY:	case LOOP:
			return true;
		case ID:
			if (!visited.contains((Id) language)) {
				visited.add((Id) language);
				return nullable(visited,((Id)language).data.right.current);
			}
		case REJECT: case ANY: case SYMBOL: default:
			return false;
		case LIST:
			return nullable(visited, ((BinaryOperator)language).data.left) &&
					nullable(visited, ((BinaryOperator)language).data.right);
		case OR:
			return nullable(visited, ((BinaryOperator)language).data.left) ||
					nullable(visited, ((BinaryOperator)language).data.right);
		}
	}
	public boolean nullable(Language<?> language) {
		return nullable(new HashSet<Id>(), language);
	}
	public boolean nullable() {
		return nullable(language());
	}
	// Is the identifier a terminal?
	private boolean terminal(Set<Id> visited, Language<?> language) {
		switch(language.type) {
		case EMPTY: case REJECT: case ANY: case SYMBOL: default:
			return true;
		case ID:
			if (!visited.contains((Id) language)) {
				visited.add((Id) language);
				return terminal(visited,((Id)language).data.right.current);
			}
			return false;
		case LIST:
			return terminal(visited, ((BinaryOperator)language).data.left) &&
					terminal(visited, ((BinaryOperator)language).data.right);
		case OR:
			return terminal(visited, ((BinaryOperator)language).data.left) &&
					terminal(visited, ((BinaryOperator)language).data.right);
		case LOOP:
			return terminal(visited, ((Loop)language).data);
		}
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
			return reject;
		case LIST:
			Language<?> result = list(derivative(visited, c, ((BinaryOperator)language).data.left),
					((BinaryOperator)language).data.right);
			if (nullable(((BinaryOperator)language).data.left)) {
				return or(result, derivative(visited, c, ((BinaryOperator)language).data.right));
			}
			return result;
		case OR:
			return or(derivative(visited, c, ((BinaryOperator)language).data.left),
					derivative(visited, c, ((BinaryOperator)language).data.right));
		case LOOP:
			return list(derivative(visited, c, ((Loop)language).data), language);
		case ANY:
			return empty;
		case SYMBOL:
			if (((Symbol)language).data == c) {
				return empty;
			}
		case REJECT: case EMPTY: default:
			return reject;
		}
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
		case EMPTY: case REJECT: default:
			return reject;
		case LIST:
			Language<?> result = first(visited, ((BinaryOperator)language).data.left);
			if (nullable(((BinaryOperator)language).data.left)) {
				result = or(result, first(visited, ((BinaryOperator)language).data.right));
			}
			return result;
		case OR:
			return or(first(visited, ((BinaryOperator)language).data.left),
					first(visited, ((BinaryOperator)language).data.right));
		case LOOP:
			return first(visited, ((Loop)language).data);
		case ANY: case SYMBOL:
			return language;
		}
	}
	public Language<?> first(Language<?> language) {
		return first(new HashSet<Id>(), language);
	}
	public Language<?> first() {
		return first(language());
	}
	public boolean matches(Language<?> language, String s) {
		Set<Id> visited = new HashSet<Id>();
		for (int i = 0; i < s.length(); i++) {
			language = derivative(visited, s.charAt(i), language);
			visited.clear();
			debug("top: ", language);
			// FIXME: Uncomment to debug
			System.out.println(show(language));
		}
		return nullable(language);
	}
	public boolean matches(String s) {
		return matches(language(), s);
	}

	public abstract Language<?> language();
}
