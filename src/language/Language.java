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
	// Match any character
	public static Any any() {
		return Any.getInstance();
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
	// Match nodes in order
	public static Node seq(Node... nodes) {
		return Sequence.getInstance(nodes, 0);
	}
	// Match language*
	public static Node many(Node regex) {
		return Star.getInstance(regex);
	}
	// Declare/use identifier
	public static Identifier let(String label) {
		return Identifier.getInstance(label);
	}
	// Declare/use identifier
	public static Identifier id(String label) {
		return Identifier.getInstance(label);
	}
	// Match a string literally
	public static Node string(String s) {
		Node[] array = new Node[s.length()];
		for (int i = 0; i < s.length(); i++) {
			array[i] = symbol(s.charAt(i));
		}
		return Sequence.getInstance(array, 0);
	}
	// Optionally match a node
	public static Node option(Node node) {
		return Or.getInstance(node, EmptyString.getInstance());
	}

	// Get the string representation of a language
	public static String asString(Node language) {
		return new Printer(language).toString();
	}
	
	// Does the language derive the empty string?
	public static boolean nullable(Node language) {
		return Nullable.nullable(language);
	}
	
	// What appears first?
	public static Node firstSet(Node language) {
		return FirstSet.firstSet(language);
	}
	// Derivative matching
	// Compute Dc(regex)
	public static Node derivative(Node language, char c) {
		return Derivative.derivative(language, c);
	}
	// Does the language match the string?
	public static boolean matches(Node language, String s) {
		for (int i = 0; i < s.length(); i++) {
			language = derivative(language, s.charAt(i));
			// FIXME: Uncomment to debug
			System.out.println(asString(language));
		}
		return nullable(language);
	}
}
