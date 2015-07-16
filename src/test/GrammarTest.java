package test;

import languageV2.Language;
import languageV2.Node;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class GrammarTest {
	
	@Test
	public void mathExpression() {
		Language g = new Language() {{
			Id expression = id("expression");
			Id term = id("term");
			Id factor = id("factor");
			Id digit = id("digit");
			Id digits = id("digits");
			derives(expression, term, many(or(symbol('+'), symbol('-')), term));
			derives(term, factor, many(or(symbol('*'), symbol('/')), factor));
			derives(factor, or(digits, list(symbol('('), expression, symbol(')'))));
			derives(digit, range('0', '9'));
			derives(digits, digit, many(digit));
			//debug = true;
		}};
		//System.out.println(g.toString());
		Assert.assertTrue(g.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1"));
		Assert.assertTrue(g.matches("(1+1+1+1+1+1+1+1+1)/(1+1+1+1+1+1+1+1+1+1+1+1)*1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1"));
		Assert.assertFalse(g.matches("1+"));
		Assert.assertFalse(g.matches("27+"));
		Assert.assertTrue(g.matches("27+34"));

	}
	
	@Test
	public void testGrammar() {
		Language g = new Language() {{
			derives("syntax", option(id("production"), id("syntax")));
			derives("production", id("identifier"), symbol('='), id("expression"), symbol('.'));
			derives("expression", option(id("expression"), symbol('|')), id("term"));
			derives("term", option(id("term")), id("factor"));
			derives("factor", or(id("identifier"), id("string")));
			derives("identifier", id("letter"), many(id("letter"), id("digit")));
			derives("string", symbol('"'), many(any), symbol('"'));
			derives("digit", range('0', '9'));
			derives("letter", or(range('A','Z'), range('a','z')));
		}};
	}
	
	@Test
	public void testEBNF() {
		Language g = new Language() {{
			Id expression = id("expression");
			derives("syntax", many(id("production")));
			derives("production", id("identifier"), symbol('='), expression, symbol('.'));
			derives("expression", id("term"), many(symbol('|'), id("term")));
			derives("term", id("factor"), many(id("factor")));
			derives("factor", or(id("identifier"),
					id("string"),
					list(symbol('('), expression, symbol(')')),
					list(symbol('['), expression, symbol(']')),
					list(symbol('{'), expression, symbol('}'))));
			derives("identifier", id("letter"), many(or(id("letter"), id("digit"))));
			derives("string", symbol('"'), many(id("character")), symbol('"'));
			derives("letter", or(range('A', 'Z'), range('a','z')));
			derives("digit", range('0', '9'));
		}};
	}
	
	@Test
	public void testCox() {
		Language g = new Language() {{
			derives("S", or(list(id("S"), symbol('+'), id("S")), symbol('1')));

			//debug = true;
		}};
		Assert.assertTrue(g.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1"));
		Assert.assertFalse(g.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1++1"));
	}

	@Test
	public void testCox2() {
		Language g = new Language() {{
			Language.Id s = id();
			derives(s, or(list(s, symbol('+'), s), symbol('1')));

			//debug = true;
		}};
		long before, after;
		before = System.nanoTime();
		Assert.assertTrue(g.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1"));
		after = System.nanoTime();
		System.out.println(after - before);
		before = System.nanoTime();
		Assert.assertFalse(g.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1++1"));
		after = System.nanoTime();
		System.out.println(after - before);
	}

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
			debug = true;
		}};
		Assert.assertTrue(g.matches("abcdefg"));
		Assert.assertFalse(g.matches(""));
		Language f = new Language() {{
			derives("hi",many(symbol('a')),many(symbol('b')));
			// debug = true;
		}};
		Assert.assertTrue(f.matches("ab"));
		Assert.assertTrue(f.matches(""));
		Assert.assertTrue(f.matches("b"));
		Assert.assertFalse(f.matches("c"));
		Assert.assertTrue(f.matches("aaaaaabbbb"));
		Assert.assertTrue(f.matches("aaaaaaaaaaaaaaaaa"));
		Assert.assertFalse(f.matches("aaaaaaaaabaaaaaaaa"));
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
			derives("S",option(id("S"),symbol('('),id("S"),symbol(')')));
		}};
//		Assert.assertTrue(parens.isNonterminal("S"));
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
			derives("L",option(id("L"),symbol('x')));
			// debug = true;
		}};
		Assert.assertTrue(g.matches("xxxx"));
		Assert.assertTrue(g.matches("xx"));
		Assert.assertTrue(g.matches(""));
		Assert.assertTrue(g.matches("x"));
		Assert.assertTrue(g.matches("xxx"));
		Assert.assertTrue(g.matches("xxxxxxxxxxxxxxxxxxxxxxx"));
		Assert.assertFalse(g.matches("L"));
	}
	
	@Test
	public void testPage148() {
		Language page148 = new Language() {{
			derives("S",id("A"), id("C"));
			derives("C",option(symbol('c')));
			derives("A",or(list(symbol('a'), id("B"), id("C"), symbol('d')), list(id("B"), id("Q"))));
			derives("B",option(symbol('b'), id("B")));
			derives("Q",option(symbol('q')));
		}};
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
			derives("Command",or(range('+','.'),symbol('<'), symbol('>')));
			// Loop -> '[' Sequence ']'
			derives("Loop",symbol('['), id("Sequence"), symbol(']'));
			// debug = true;
		}};
		Assert.assertTrue(g.matches("+"));
		Assert.assertTrue(g.matches("++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++."));
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
			derives("regex",id("term"),many(symbol('|'),id("regex")));
			derives("term",many(id("factor")));
			derives("factor",id("base"), option(symbol('*')));
			derives("base",or(list(option(symbol('\\')), any), list(symbol('('), id("regex"), symbol(')'))));
			debug = true;
		}};
		Assert.assertTrue(regex.matches("a"));
		Assert.assertTrue(regex.matches("a|b"));
		Assert.assertTrue(regex.matches("a|b**"));
		Assert.assertTrue(regex.matches("(hello)|(world)"));
	}
	
	@After
	public void summary() {
		// 2335 total
		System.out.println(Node.allocations);
	}
}
