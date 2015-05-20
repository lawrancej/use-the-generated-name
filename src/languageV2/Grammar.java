package languageV2;

import java.util.HashMap;
import java.util.Map;

public abstract class Grammar {
	public static enum Construct {
		ANY /* . */,
		REJECT /* empty set */,
		EMPTY /* empty string */,
		SYMBOL /* c */,
		STAR /* a* */,
		OR /* a|b */,
		LIST /* ab */,
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
	public static class UnaryOperator extends Language<Language<?>> {
		UnaryOperator(Construct type, Language<?> data) {
			super(type, data);
		}
	}
	public class Id extends Language<Pair<String,Mutable<Language<?>>>> {
		Id(String label) {
			super(Construct.ID, new Pair<String, Mutable<Language<?>>>(label, new Mutable<Language<?>>(reject)));
		}
		public void derive(Language<?>... languages) {
			data.right.current = or(data.right.current, list(languages));
		}
	}
	/* Singletons */
	public static Language<Void> any = new Language<Void>(Construct.ANY, null);
	public static Language<Void> reject = new Language<Void>(Construct.REJECT, null);
	public static Language<Void> empty = new Language<Void>(Construct.EMPTY, null);
	/* Flyweights */
	/* Pattern: map what's inside the language to the language */
	private Map<Character, Grammar.Symbol> symbols = new HashMap<Character, Grammar.Symbol>();
	private Map<Language<?>, UnaryOperator> stars = new HashMap<Language<?>, UnaryOperator>();
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
		if (!stars.containsKey(language)) {
			stars.put(language, new UnaryOperator(Construct.STAR, language));
		}
		return stars.get(language);
	}
	private Language<?> orInstance(Language<?> left, Language <?> right) {
		if (left == reject) { return right; }
		if (right == reject) { return left; }
		if (left == right) { return left; }
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
			show(buffer,((BinaryOperator)language).data.left);
			buffer.append(' ');
			show(buffer,((BinaryOperator)language).data.right);
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
		case STAR:
			buffer.append('(');
			show(buffer,((UnaryOperator)language).data);
			buffer.append(")*");
			break;
		case SYMBOL:
			buffer.append(((Grammar.Symbol)language).data);
			break;
		default:
			break;
		}
		return buffer;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (language().type == Construct.ID) {
			for (Id id : ids.values()) {
				buffer.append(id.data.left);
				buffer.append(" ::= ");
				show(buffer,id.data.right.current);
				buffer.append("\n");
			}
			return buffer.toString();
		} else {
			return show(buffer, language()).toString();
		}
	}
	public abstract Language<?> language();
}
