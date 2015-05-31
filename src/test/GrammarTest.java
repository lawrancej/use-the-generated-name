package test;

import languageV2.Grammar;

import org.junit.Assert;
import org.junit.Test;

public class GrammarTest {

	@Test
	public void testSymbol() {
		Grammar g = new Grammar() {{
			define(symbol('s'));
		}};
		Assert.assertFalse(g.nullable());
		Assert.assertEquals(Grammar.reject, g.derivative('e'));
		Assert.assertEquals(Grammar.empty, g.derivative('s'));
		Assert.assertTrue(g.matches("s"));
	}

	@Test
	public void testMany() {
		Grammar g = new Grammar() {{
			define(list(any, many(any)));
		}};
		Assert.assertTrue(g.matches("abcdefg"));
		Assert.assertFalse(g.matches(""));
	}

	@Test
	public void testOr() {
		Grammar aaaa = new Grammar() {{
			define(many(or(symbol('a'), symbol('b'))));
		}};
		Assert.assertFalse(aaaa.matches("abcdefg"));
		Assert.assertTrue(aaaa.matches("aaaa"));
		Assert.assertTrue(aaaa.matches(""));
		Assert.assertTrue(aaaa.matches("aabbabab"));
	}

	@Test
	public void testList() {
		Grammar g = new Grammar() {{
			define(list(symbol('a'), symbol('b')));
		}};
		Assert.assertTrue(g.matches("ab"));
		Assert.assertFalse(g.matches("aaaa"));
		Assert.assertFalse(g.matches(""));
		Assert.assertFalse(g.matches("aabbabab"));
	}
	
	@Test
	public void testAny() {
		Grammar g = new Grammar() {{
			define(list(any, many(any), symbol('b')));
		}};
		Assert.assertTrue(g.matches("jebb"));
		Assert.assertFalse(g.matches("jabba"));
	}
	
	@Test
	public void testParens() {
		Grammar parens = new Grammar() {{
			derives("S",id("S"),symbol('('),id("S"),symbol(')'));
			derives("S");
			define(id("S"));
		}};
		Assert.assertTrue(parens.isNonterminal("S"));
		Assert.assertFalse(parens.matches("("));
		Assert.assertTrue(parens.matches("()"));
		Assert.assertFalse(parens.matches(")"));
		Assert.assertTrue(parens.matches(parens.first(), "("));
		Assert.assertFalse(parens.matches(parens.first(), ")"));
	}
	
	@Test
	public void testHelloWorld() {
		Grammar g = new Grammar() {{
			define(string("hello world"));
		}};
		Assert.assertTrue(g.matches("hello world"));
		Assert.assertFalse(g.matches("hello"));
	}
	
	@Test
	public void testFooBarFrak() {
		Grammar g = new Grammar() {{
			define(many(or(string("foo"),string("bar"),string("frak"))));
		}};
		Assert.assertTrue(g.matches("foo"));
		Assert.assertTrue(g.matches("foofoobar"));
		Assert.assertFalse(g.matches("foobaz"));
	}
	
	@Test
	public void testLeftRecursion() {
		Grammar g = new Grammar() {{
			id("L").derives(id("L"),symbol('x'));
			id("L").derives();
			define(id("L"));
		}};
		Assert.assertTrue(g.isNonterminal("L"));
		Assert.assertTrue(g.matches("xx"));
		Assert.assertTrue(g.matches(""));
		Assert.assertTrue(g.matches("x"));
		Assert.assertFalse(g.matches("L"));
	}
	
	@Test
	public void testNonterminal() {
		Grammar g = new Grammar() {{
			id("S").derives(or(id("A"), id("nope")));
			id("A").derives(many(id("S")));
			id("nope").derives(any);
			define("S");
		}};
		Assert.assertTrue(g.isNonterminal("S"));
		Assert.assertTrue(g.isNonterminal("A"));
		Assert.assertFalse(g.isNonterminal("nope"));
	}

	
	@Test
	public void testNonterminal2() {
		Grammar g = new Grammar() {{
			id("S").derives(or(many(id("S")), id("nope")));
			id("nope").derives(any);
			define("S");
		}};
		Assert.assertTrue(g.isNonterminal("S"));
		Assert.assertFalse(g.isNonterminal("nope"));
	}
	
	@Test
	public void testPage148() {
		Grammar page148 = new Grammar() {{
			id("S").derives(id("A"), id("C"));
			id("C").derives(symbol('c'));
			id("C").derives();
			id("A").derives(symbol('a'), id("B"), id("C"), symbol('d'));
			id("A").derives(id("B"), id("Q"));
			id("B").derives(symbol('b'), id("B"));
			id("B").derives();
			id("Q").derives(symbol('q'));
			id("Q").derives();
			define("S");
		}};
		System.out.println(page148);
//		System.out.println(page148.show(page148.first(page148.id("A"))));
		Assert.assertTrue(page148.nullable());
	}

	@Test
	public void testBrainfuck() {
		Grammar g = new Grammar() {{
			// Program -> Sequence
			id("Program").derives(id("Sequence"));
			// Sequence -> ( Command | Loop ) *
			id("Sequence").derives(many(or(id("Command"), id("Loop"))));
			// Command -> '+' | '-' | '<' | '>' | ',' | '.'
			id("Command").derives(or(
				symbol('+'),symbol('-'),
				symbol('<'), symbol('>'),
				symbol('.'), symbol(',')
			));
			// Loop -> '[' Sequence ']'
			id("Loop").derives(symbol('['), id("Sequence"), symbol(']'));
			define("Program");
		}};
		Assert.assertTrue(g.matches("+"));
		Assert.assertFalse(g.matches("+["));
		Assert.assertFalse(g.matches("+[."));
		Assert.assertFalse(g.matches("+[.+"));
		Assert.assertFalse(g.matches("hi"));
		Assert.assertTrue(g.matches("+[.+]"));
		Assert.assertTrue(g.matches("+[.+]+"));
		Assert.assertFalse(g.matches("boo"));
	}

	@Test
	public void testRegexGrammar() {
		Grammar regex = new Grammar() {{
			id("regex").derives(id("term"),symbol('|'),id("regex"));
			id("regex").derives(id("term"));
			id("term").derives(many(id("factor")));
			id("factor").derives(id("base"), option(symbol('*')));
			id("base").derives(any);
			id("base").derives(symbol('\\'), any);
			id("base").derives(symbol('('), id("regex"), symbol(')'));
			define(id("regex"));
			debug = true;
		}};
		System.out.println(regex);
		Assert.assertTrue(regex.isNonterminal("regex"));
		Assert.assertTrue(regex.isNonterminal("term"));
		Assert.assertTrue(regex.isNonterminal("factor"));
		Assert.assertTrue(regex.isNonterminal("base"));
		Assert.assertTrue(regex.matches("a"));
		Assert.assertTrue(regex.matches("a|b"));
		Assert.assertTrue(regex.matches("a|b**"));
		Assert.assertTrue(regex.matches("(hello)|(world)"));
	}
}
