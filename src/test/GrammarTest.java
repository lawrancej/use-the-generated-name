package test;

import languageV2.Language;

import org.junit.Assert;
import org.junit.Test;

public class GrammarTest {

	@Test
	public void testSymbol() {
		Language g = new Language() {{
			define(symbol('s'));
		}};
		Assert.assertFalse(g.nullable());
		Assert.assertEquals(Language.reject, g.derivative('e'));
		Assert.assertEquals(Language.empty, g.derivative('s'));
		Assert.assertTrue(g.matches("s"));
	}

	@Test
	public void testMany() {
		Language g = new Language() {{
			define(list(any, many(any)));
		}};
		Assert.assertTrue(g.matches("abcdefg"));
		Assert.assertFalse(g.matches(""));
	}

	@Test
	public void testOr() {
		Language aaaa = new Language() {{
			define(many(or(symbol('a'), symbol('b'))));
		}};
		Assert.assertFalse(aaaa.matches("abcdefg"));
		Assert.assertTrue(aaaa.matches("aaaa"));
		Assert.assertTrue(aaaa.matches(""));
		Assert.assertTrue(aaaa.matches("aabbabab"));
	}

	@Test
	public void testList() {
		Language g = new Language() {{
			define(list(symbol('a'), symbol('b')));
		}};
		Assert.assertTrue(g.matches("ab"));
		Assert.assertFalse(g.matches("aaaa"));
		Assert.assertFalse(g.matches(""));
		Assert.assertFalse(g.matches("aabbabab"));
	}
	
	@Test
	public void testAny() {
		Language g = new Language() {{
			define(list(any, many(any), symbol('b')));
		}};
		Assert.assertTrue(g.matches("jebb"));
		Assert.assertFalse(g.matches("jabba"));
	}
	
	@Test
	public void testParens() {
		Language parens = new Language() {{
			derives("S",id("S"),symbol('('),id("S"),symbol(')'));
			derives("S");
			debug = true;
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
		Language g = new Language() {{
			define(string("hello world"));
		}};
		Assert.assertTrue(g.matches("hello world"));
		Assert.assertFalse(g.matches("hello"));
	}
	
	@Test
	public void testFooBarFrak() {
		Language g = new Language() {{
			define(many(or(string("foo"),string("bar"),string("frak"))));
		}};
		Assert.assertTrue(g.matches("foo"));
		Assert.assertTrue(g.matches("foofoobar"));
		Assert.assertFalse(g.matches("foobaz"));
	}
	
	@Test
	public void testLeftRecursion() {
		Language g = new Language() {{
			derives("L",id("L"),symbol('x'));
			derives("L");
//			debug = true;
		}};
		System.out.println(g);
		Assert.assertTrue(g.isNonterminal("L"));
		Assert.assertTrue(g.matches("xx"));
		Assert.assertTrue(g.matches(""));
		Assert.assertTrue(g.matches("x"));
		Assert.assertFalse(g.matches("L"));
	}
	
	@Test
	public void testNonterminal() {
		Language g = new Language() {{
			derives("S",or(id("A"), id("nope")));
			derives("A",many(id("S")));
			derives("nope",any);
		}};
		Assert.assertTrue(g.isNonterminal("S"));
		Assert.assertTrue(g.isNonterminal("A"));
		Assert.assertFalse(g.isNonterminal("nope"));
	}

	
	@Test
	public void testNonterminal2() {
		Language g = new Language() {{
			derives("S",or(many(id("S")), id("nope")));
			derives("nope",any);
		}};
		Assert.assertTrue(g.isNonterminal("S"));
		Assert.assertFalse(g.isNonterminal("nope"));
	}
	
	@Test
	public void testPage148() {
		Language page148 = new Language() {{
			derives("S",id("A"), id("C"));
			derives("C",symbol('c'));
			derives("C");
			derives("A",symbol('a'), id("B"), id("C"), symbol('d'));
			derives("A",id("B"), id("Q"));
			derives("B",symbol('b'), id("B"));
			derives("B");
			derives("Q",symbol('q'));
			derives("Q");
		}};
		System.out.println(page148);
//		System.out.println(page148.show(page148.first(page148.id("A"))));
		Assert.assertTrue(page148.nullable());
	}

	@Test
	public void testBrainfuck() {
		Language g = new Language() {{
			// Program -> Sequence
			derives("Program",id("Sequence"));
			// Sequence -> ( Command | Loop ) *
			derives("Sequence",many(or(id("Command"), id("Loop"))));
			// Command -> '+' | '-' | '<' | '>' | ',' | '.'
			derives("Command",or(
				symbol('+'),symbol('-'),
				symbol('<'), symbol('>'),
				symbol('.'), symbol(',')
			));
			// Loop -> '[' Sequence ']'
			derives("Loop",symbol('['), id("Sequence"), symbol(']'));
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
		Language regex = new Language() {{
			derives("regex",id("term"),symbol('|'),id("regex"));
			derives("regex",id("term"));
			derives("term",many(id("factor")));
			derives("factor",id("base"), option(symbol('*')));
			derives("base",any);
			derives("base",symbol('\\'), any);
			derives("base",symbol('('), id("regex"), symbol(')'));
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
