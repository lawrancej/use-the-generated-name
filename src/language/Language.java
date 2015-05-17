package language;

// Programming interface
public class Language {
	// Build languages
	
	// Language primitives
	// Reject everything
	public static EmptySet reject() {
		return EmptySet.getInstance();
	}
	// Empty string
	public static EmptyString empty() {
		return EmptyString.getInstance();
	}
	// A symbol
	public static Symbol symbol(char c) {
		return Symbol.getInstance(c);
	}
	
	// Language operators
	// Match left|right|extra...
	public static Node or(Node left, Node right, Node... extra) {
		Node result = Or.getInstance(left, right);
		if (extra.length == 0) {
			return result;
		}
		for (int i = 0; i < extra.length; i++) {
			result = Or.getInstance(result, extra[i]);
		}
		return result;
	}
	// Match left followed by right followed by extra...
	public static Node seq(Node left, Node right, Node... extra) {
		Node result = Sequence.getInstance(left, right);
		if (extra.length == 0) {
			return result;
		}
		for (int i = 0; i < extra.length; i++) {
			result = Sequence.getInstance(result, extra[i]);
		}
		return result;
	}
	public static Nonterminal let(String label) {
		return Nonterminal.getInstance(label);
	}
	public static Nonterminal nonterm(String label) {
		return Nonterminal.getInstance(label);
	}
	// Match language*
	public static Node many(Node regex) {
		return Star.getInstance(regex);
	}
	// Match a string literally
	public static Node string(String s) {
		if (s.length() == 0) {
			return EmptyString.getInstance();
		}
		Node result = symbol(s.charAt(0));
		for (int i = 1; i < s.length(); i++) {
			result = seq(result, symbol(s.charAt(i)));
		}
		return result;
	}
	public static Node option(Node node) {
		return Or.getInstance(node, EmptyString.getInstance());
	}

	// Get the string representation of a language
	public static String asString(Node node) {
		return new Printer(node).toString();
	}
	
	// Is the language nullable?
	public static boolean nullable(Node node) {
		return node.accept(new Nullable());
	}
	
	// Derivative matching
	private static Derivative derivative = new Derivative();
	// Compute Dc(regex)
	public static Node derivative(Node regex, char c) {
		derivative.c = c;
		return regex.accept(derivative);
	}
	// Does the language match the string?
	public static boolean matches(Node regex, String s) {
		for (int i = 0; i < s.length(); i++) {
			regex = derivative(regex, s.charAt(i));
			// FIXME: Uncomment to debug
			// System.out.println(asString(regex));
		}
		return nullable(regex);
	}
}
