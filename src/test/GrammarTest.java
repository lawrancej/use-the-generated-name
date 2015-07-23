package test;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dictorobitary.Language;
import com.dictorobitary.Node;

public class GrammarTest {
	static Language mathExpression;
	static Language grammar;
	static Language identifier;
	static Language ebnf;
	static Language cox;
	static Language cox2;
	static Language symbol;
	static Language rpn;
	static Language rpn2;
	static Language regex;
	@BeforeClass
	public static void setup() {
		long before, after;
		before = System.nanoTime();
		mathExpression = new Language() {{
			Node<String,Void> expression = id("expression");
			Node<String,Void> term = id("term");
			Node<String,Void> factor = id("factor");
			Node<String,Void> digit = id("digit");
			Node<String,Void> digits = id("digits");
			rule(expression, term, many(or(symbol('+'), symbol('-')), term));
			rule(term, factor, many(or(symbol('*'), symbol('/')), factor));
			rule(factor, or(digits, list(symbol('('), expression, symbol(')'))));
			rule(digit, range('0', '9'));
			rule(digits, digit, many(digit));
		}};
		grammar = new Language() {{
			rule("syntax", option(id("production"), id("syntax")));
			rule("production", id("identifier"), symbol('='), id("expression"), symbol('.'));
			rule("expression", option(id("expression"), symbol('|')), id("term"));
			rule("term", option(id("term")), id("factor"));
			rule("factor", or(id("identifier"), id("string")));
			rule("identifier", id("letter"), many(id("letter"), id("digit")));
			rule("string", symbol('"'), many(any), symbol('"'));
			rule("digit", range('0', '9'));
			rule("letter", or(range('A','Z'), range('a','z')));
		}};
		identifier = new Language() {{
			// [A-Za-z][A-Za-z0-9]*
			define(or(range('A','Z'), range('a','z')), many(or(range('A','Z'), range('a','z'), range('0', '9'))));
		}};
		ebnf = new Language() {{
			Node<String,Void> expression = id("expression");
			rule("syntax", many(id("production")));
			rule("production", id("identifier"), symbol('='), expression, symbol('.'));
			rule("expression", id("term"), many(symbol('|'), id("term")));
			rule("term", id("factor"), many(id("factor")));
			rule("factor", or(id("identifier"),
					id("string"),
					list(symbol('('), expression, symbol(')')),
					list(symbol('['), expression, symbol(']')),
					list(symbol('{'), expression, symbol('}'))));
			rule("identifier", id("letter"), many(or(id("letter"), id("digit"))));
			rule("string", symbol('"'), many(id("character")), symbol('"'));
			rule("letter", or(range('A', 'Z'), range('a','z')));
			rule("digit", range('0', '9'));
		}};
		cox = new Language() {{
			rule("S", or(list(id("S"), symbol('+'), id("S")), symbol('1')));
		}};
		cox2 = new Language() {{
			Node<String,Void> s = id();
			rule(s, or(list(s, symbol('+'), s), symbol('1')));
		}};
		symbol = new Language() {{
			define(symbol('s'));
		}};
		rpn = new Language() {{
			separator(many(symbol(' ')));
			Node<String,Void> expression = id("expression");
			Node<String,Void> number = id("number");
			rule (expression, or(number, list(expression, expression, oneOf("+-/*"))));
			token (number, many1(range('0','9')));
		}};
		rpn2 = new Language() {{
			separator(many(symbol(' ')));
			Node<String,Void> expression = id("expression");
			Node<String,Void> plus = id("plus");
			Node<String,Void> minus = id("minus");
			Node<String,Void> div = id("div");
			Node<String,Void> times = id("times");
			Node<String, Void> number = id("number");
			rule (expression, or(number, plus, minus, div, times));
			rule (plus, expression, expression, symbol('+'));
			rule (minus, expression, expression, symbol('-'));
			rule (div, expression, expression, symbol('/'));
			rule (times, expression, expression, symbol('*'));
			token (number, many1(range('0','9')));
		}};
		regex = new Language() {{
			rule("regex",id("term"),many(symbol('|'),id("regex")));
			rule("term",many(id("factor")));
			rule("factor",id("base"), option(symbol('*')));
			rule("base",or(list(option(symbol('\\')), any), list(symbol('('), id("regex"), symbol(')'))));
			//get.debug = true;
		}};
		after = System.nanoTime();
		System.out.format("Setup time: %.2f milliseconds\n", (after - before)/1000000.0);
	}
	
	@Test
	public void testToken() {
		Assert.assertFalse(mathExpression.get.token.compute());
		Assert.assertTrue(mathExpression.get.token.compute(mathExpression.id("digit")));
		Assert.assertTrue(mathExpression.get.token.compute(mathExpression.id("digits")));
		Assert.assertFalse(mathExpression.get.token.compute(mathExpression.id("term")));
		Assert.assertFalse(mathExpression.get.token.compute(mathExpression.id("factor")));
	}
	
	@Test
	public void testMathExpression() {
/*		for (int i = 0; i < 10; i++) {
			String s = mathExpression.get.generator.compute().toString();
			Assert.assertTrue(mathExpression.get.matches(s));
			System.out.println(s);
		}
*/
		for (int i = 0; i < 00; i++) {
			String s = mathExpression.get.generator.compute(2,3).toString();
			boolean result = mathExpression.get.matches(s);
			if (!result) {
				// WTF?
				System.out.println(s);
			}
			Assert.assertTrue(result);
		}
//		System.out.println(mathExpression.get.generator.compute().toString());
		for (int i = 0; i < 49; i++) {
		Assert.assertTrue(mathExpression.get.matches("(0/(7*07+22)-(5))"));
		Assert.assertTrue(mathExpression.get.matches("4*(72+(16*7+50)/2)"));
		Assert.assertTrue(mathExpression.get.matches("(((81/08)*4+5*1))/43+28"));
		Assert.assertTrue(mathExpression.get.matches("(58*05+34*86)/4"));
		Assert.assertTrue(mathExpression.get.matches("(48)-5*6"));
		Assert.assertTrue(mathExpression.get.matches("(14-4)*2-4/7"));
		}
		Assert.assertTrue(mathExpression.get.matches("(0/(7*07+22)-(5))"));
		Assert.assertTrue(mathExpression.get.matches("4*(72+(16*7+50)/2)"));
		Assert.assertTrue(mathExpression.get.matches("(((81/08)*4+5*1))/43+28"));
		System.out.println("It's about to break");
		System.out.println(mathExpression.get.gv.compute());
		Assert.assertTrue(mathExpression.get.matches("(58*05+34*86)/4"));
		Assert.assertFalse(mathExpression.get.matches("((2)*77*40)3"));
		//System.out.println(g.toString());
		Assert.assertTrue(mathExpression.get.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1"));
		Assert.assertTrue(mathExpression.get.matches("(1+1+1+1+1+1+1+1+1)/(1+1+1+1+1+1+1+1+1+1+1+1)*1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1"));
		Assert.assertFalse(mathExpression.get.matches("1+"));
		Assert.assertFalse(mathExpression.get.matches("27+"));
		Assert.assertTrue(mathExpression.get.matches("27+34"));
	}
	
//	@Test
	public void testRPN2() {

		Assert.assertTrue(rpn2.get.matches("2"));
		Assert.assertTrue(rpn2.get.matches(" 2"));
		Assert.assertTrue(rpn2.get.matches(" 20"));
		Assert.assertTrue(rpn2.get.matches("2 "));
		Assert.assertTrue(rpn2.get.matches("2 2 +"));
		Assert.assertTrue(rpn2.get.matches("2 2 2 + -"));
		Assert.assertTrue(rpn2.get.matches("2 2 + 2 -"));
		Assert.assertTrue(rpn2.get.matches("2 3 -"));
		Assert.assertTrue(rpn2.get.matches("2 3 3 - -"));
		Assert.assertTrue(rpn2.get.matches("2 3 3 - 3 - *"));
		Assert.assertTrue(rpn2.get.matches("2 1 /"));
	}
	
//	@Test
	public void testRPN() {
		Assert.assertTrue(rpn.get.matches("2"));
		Assert.assertTrue(rpn.get.matches(" 2"));
		Assert.assertTrue(rpn.get.matches("2 "));
		Assert.assertTrue(rpn.get.matches("2 2 +"));
		Assert.assertTrue(rpn.get.matches("2 3 -"));
		Assert.assertTrue(rpn.get.matches("2 3 3 - -"));
		Assert.assertTrue(rpn.get.matches("2 3 3 - 3 - *"));
		Assert.assertTrue(rpn.get.matches("2 1 /"));
	}
	
	@Test
	public void testGrammar() {
	}
	
	@Test
	public void testIdentifier() {
		Assert.assertFalse(identifier.get.matches("4chan"));
		Assert.assertFalse(identifier.get.matches("2pac"));
		Assert.assertTrue(identifier.get.matches("x"));
		Assert.assertTrue(identifier.get.matches("xyzzy3"));
	}
	
	@Test
	public void testEBNF() {
	}
	
	@Test
	public void testCox() {
		Assert.assertTrue(cox.get.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1"));
		Assert.assertFalse(cox.get.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1++1"));
	}

	@Test
	public void testCox2() {
		long before, after;
		before = System.nanoTime();
		Assert.assertTrue(cox2.get.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1"));
		after = System.nanoTime();
		System.out.println(after - before);
		before = System.nanoTime();
		Assert.assertFalse(cox2.get.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1++1"));
		after = System.nanoTime();
		System.out.println(after - before);
	}

	@Test
	public void testSymbol() {
		Assert.assertFalse(symbol.get.nullable.compute());
		Assert.assertFalse(symbol.get.matches("e"));
		Assert.assertTrue(symbol.get.matches("s"));
		Assert.assertTrue(symbol.get.matches("s"));
	}

	@Test
	public void testMany() {
		Language g = new Language() {{
			define(list(any, many(any)));
		}};
		Assert.assertTrue(g.get.matches("abcdefg"));
		Assert.assertFalse(g.get.matches(""));
		Language f = new Language() {{
			rule("hi",many(symbol('a')),many(symbol('b')));
		}};
		Assert.assertTrue(f.get.matches("ab"));
		Assert.assertTrue(f.get.matches(""));
		Assert.assertTrue(f.get.matches("b"));
		Assert.assertFalse(f.get.matches("c"));
		Assert.assertTrue(f.get.matches("aaaaaabbbb"));
		Assert.assertTrue(f.get.matches("aaaaaaaaaaaaaaaaa"));
		Assert.assertFalse(f.get.matches("aaaaaaaaabaaaaaaaa"));
	}

	@Test
	public void testOr() {
		Language aaaa = new Language() {{
			define(many(or(symbol('a'), symbol('b'))));
		}};
		Assert.assertFalse(aaaa.get.matches("abcdefg"));
		Assert.assertTrue(aaaa.get.matches("aaaa"));
		Assert.assertTrue(aaaa.get.matches(""));
		Assert.assertTrue(aaaa.get.matches("aabbabab"));
	}

	@Test
	public void testList() {
		Language g = new Language() {{
			define(list(symbol('a'), symbol('b')));
		}};
		Assert.assertTrue(g.get.matches("ab"));
		Assert.assertFalse(g.get.matches("aaaa"));
		Assert.assertFalse(g.get.matches(""));
		Assert.assertFalse(g.get.matches("aabbabab"));
	}
	
	@Test
	public void testAny() {
		Language g = new Language() {{
			define(list(any, many(any), symbol('b')));
		}};
		Assert.assertTrue(g.get.matches("jebb"));
		Assert.assertFalse(g.get.matches("jabba"));
	}
	
	@Test
	public void testParens() {
		Language parens = new Language() {{
			rule("S",option(id("S"),symbol('('),id("S"),symbol(')')));
		}};
//		Assert.assertTrue(parens.isNonterminal("S"));
		Assert.assertFalse(parens.get.matches("("));
		Assert.assertTrue(parens.get.matches("()"));
		Assert.assertFalse(parens.get.matches(")"));
		Assert.assertTrue(parens.get.matches(parens.get.firstSet.compute(), "("));
		Assert.assertFalse(parens.get.matches(parens.get.firstSet.compute(), ")"));
	}
	
	@Test
	public void testHelloWorld() {
		Language g = new Language() {{
			define(string("hello world"));
		}};
		Assert.assertTrue(g.get.matches("hello world"));
		Assert.assertFalse(g.get.matches("hello"));
	}
	
	@Test
	public void testFooBarFrak() {
		Language g = new Language() {{
			define(many(or(string("foo"),string("bar"),string("frak"))));
		}};
		Assert.assertTrue(g.get.matches("foo"));
		Assert.assertTrue(g.get.matches("foofoobar"));
		Assert.assertFalse(g.get.matches("foobaz"));
	}
	
	@Test
	public void testLeftRecursion() {
		Language g = new Language() {{
			rule("L",option(id("L"),symbol('x')));
			// debug = true;
		}};
		Assert.assertTrue(g.get.matches("xxxx"));
		Assert.assertTrue(g.get.matches("xx"));
		Assert.assertTrue(g.get.matches(""));
		Assert.assertTrue(g.get.matches("x"));
		Assert.assertTrue(g.get.matches("xxx"));
		Assert.assertTrue(g.get.matches("xxxxxxxxxxxxxxxxxxxxxxx"));
		Assert.assertFalse(g.get.matches("L"));
	}
	
	@Test
	public void testPage148() {
		Language page148 = new Language() {{
			rule("S",id("A"), id("C"));
			rule("C",option(symbol('c')));
			rule("A",or(list(symbol('a'), id("B"), id("C"), symbol('d')), list(id("B"), id("Q"))));
			rule("B",option(symbol('b'), id("B")));
			rule("Q",option(symbol('q')));
		}};
//		System.out.println(page148.show(page148.first(page148.id("A"))));
		Assert.assertTrue(page148.get.nullable.compute());
	}

	@Test
	public void testBrainfuck() {
		Language g = new Language() {{
			// Program -> Sequence
			rule("Program",id("Sequence"));
			// Sequence -> ( Command | Loop ) *
			rule("Sequence",many(or(id("Command"), id("Loop"))));
			// Command -> '+' | '-' | '<' | '>' | ',' | '.'
			rule("Command",oneOf("+-<>,."));
			// Loop -> '[' Sequence ']'
			rule("Loop",symbol('['), id("Sequence"), symbol(']'));
		}};
		Assert.assertTrue(g.get.matches("+"));
		Assert.assertTrue(g.get.matches("++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++."));
		Assert.assertFalse(g.get.matches("+["));
		Assert.assertFalse(g.get.matches("+[."));
		Assert.assertFalse(g.get.matches("+[.+"));
		Assert.assertFalse(g.get.matches("hi"));
		Assert.assertTrue(g.get.matches("+[.+]"));
		Assert.assertTrue(g.get.matches("+[.+]+"));
		Assert.assertFalse(g.get.matches("boo"));
	}

	@Test
	public void testRegexGrammar() {
		Assert.assertTrue(regex.get.matches("a"));
		Assert.assertTrue(regex.get.matches("a|b"));
		Assert.assertTrue(regex.get.matches("a|b**"));
		Assert.assertTrue(regex.get.matches("(hello)|(world)"));
	}
	
//	@After
	public void summary() {
		// 3528 total
		System.out.println(Node.allocations);
	}
}
