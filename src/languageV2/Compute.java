package languageV2;

import languageV2.traversal.Derivative;
import languageV2.traversal.GraphViz;
import languageV2.traversal.Nullable;
import languageV2.traversal.Printer;

/**
 * Compute with a language specification.
 * 
 * @author Joey Lawrance
 *
 */
final public class Compute {
	final private Language g;
	final public Nullable nullable;
	final public Derivative derivative;
	final public GraphViz gv;
	final public Printer printer;
	public Compute(Language language) {
		g = language;
		nullable = new Nullable(g);
		derivative = new Derivative(g);
		gv = new GraphViz(g);
		printer = new Printer(g);
	}
	public boolean debug = false;
	public boolean matches(Node<?,?> language, CharSequence s) {
		boolean result;
		if (debug) {
			System.out.println(gv.compute());
			System.out.format("Nodes %d, edges %d\n", gv.nodes(), gv.edges());
		}
		for (int i = 0; i < s.length(); i++) {
			derivative.c = s.charAt(i);
			language = derivative.compute(language);
			if (language == Language.reject) {
				System.out.format("Syntax error at character '%c', index %d in string: %s\n", s.charAt(i), i, s);
				break;
			}
			if (debug) {
				System.out.println(gv.compute(language));
				System.out.format("Nodes %d, edges %d\n", gv.nodes(), gv.edges());
			}
		}
		if (debug) {
			System.out.println(gv.compute(language));
			System.out.println(printer.compute(language));
		}
		result = nullable.compute(language);
		return result;
	}
	public boolean matches(CharSequence s) {
		return matches(g.definition(), s);
	}

}
