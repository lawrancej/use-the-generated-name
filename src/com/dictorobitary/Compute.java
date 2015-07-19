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
	public Compute(Language language) {
		g = language;
		derivative = new Derivative(g);
		firstSet = new FirstSet(g);
		generator = new Generator(g);
		gv = new GraphViz(g);
		nullable = new Nullable(g);
		printer = new Printer(g);
	}
	public boolean debug = false;
	/**
	 * Begin traversal of the language specification, at specified identifier
	 * @param visitor
	 * @param id
	 */
	public <T> T beginTraversal(Visitor<T> visitor, String id) {
		return beginTraversal(visitor, g.id(id));
	}
	/**
	 * Begin traversal of a language
	 * @param visitor
	 * @param language
	 * @return
	 */
	public <T> T beginTraversal(Visitor<T> visitor, Node<?,?> language) {
		assert visitor != null;
		assert language != null;
		visitor.getWorkList().clear();
		visitor.begin();
		T accumulator;
		// Visit a grammar
		if (language.tag == Node.Tag.ID) {
			visitor.getWorkList().todo((Node<String,Void>)language);
			accumulator = visitor.bottom();
			for (Node<String,Void> identifier : visitor.getWorkList()) {
				accumulator = visitor.reduce(accumulator, g.acceptRule(visitor, identifier));
				if (visitor.done(accumulator)) {
					return visitor.end(accumulator);
				}
			}
		}
		// Visit a regex
		else {
			accumulator = g.accept(visitor, language);
		}
		return visitor.end(accumulator);
	}
	/**
	 * Begin traversal of the language specification
	 * @param visitor
	 * @return
	 */
	public <T> T beginTraversal(Visitor<T> visitor) {
		return beginTraversal(visitor, g.definition());
	}
	
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
