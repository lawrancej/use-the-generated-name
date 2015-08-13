package com.dictorobitary;

import com.dictorobitary.traversal.*;

/**
 * Accept visitors into a language specification.
 * 
 * @author Joey Lawrance
 *
 */
final public class Compute {
	final private Language g;
	final public Derivative derivative;
	final public FirstSet firstSet;
	final public Generator generator;
	final public GraphViz gv;
	final public Nullable nullable;
	final public Printer printer;
	final public Token token;
	public Compute(Language language) {
		g = language;
		derivative = new Derivative(g);
		firstSet = new FirstSet(g);
		generator = new Generator(g);
		gv = new GraphViz(g);
		nullable = new Nullable(g);
		printer = new Printer(g);
		token = new Token(g);
	}
	public boolean debug = false;
	public boolean matches(int language, CharSequence s) {
		boolean result;
		int before = language;
		if (debug) {
			System.out.println(gv.compute());
			System.out.format("Nodes %d, edges %d\n", gv.nodes(), gv.edges());
		}
		int i;
		for (i = 0; i < s.length(); i++) {
			derivative.c = s.charAt(i);
			before = language;
			language = derivative.compute(language);
			if (language == g.reject) {
				System.out.format("Syntax error at character '%c', index %d in string: %s\n", s.charAt(i), i, s);
				if (debug) {
					System.out.println(gv.compute(before));
					System.out.format("Nodes %d, edges %d\n", gv.nodes(), gv.edges());
				}
				return false;
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
		if (!result) {
			System.out.format("Syntax error at eof, index %d in string: %s\n", i, s);
			if (debug) {
				System.out.println(gv.compute(language));
				System.out.format("Nodes %d, edges %d\n", gv.nodes(), gv.edges());
			}
		}
		return result;
	}
	public boolean matches(CharSequence s) {
		return matches(g.definition(), s);
	}
}
