package com.dictorobitary;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
@RunWith(JUnit4.class)
public class GrammarTest {
	static long characters = 0;
	static Language fooBarFrak, helloWorld, aaaa, many1any, ab, asbs, asbs2,
	parens, endsWithB, identifier, page148, mathExpression, grammar, ebnf, cox,
	cox2, symbol, rpn, rpn2, regex, brainfuck, leftRecursion;
	static Language[] regularLanguages;
	static Language[] languages;
	@BeforeClass
	public static void setup() {
		long before, after;
		before = System.nanoTime();
		// Grammars
		asbs = new Language("a*b*") {{
			rule("hi",many(symbol('a')),many(symbol('b')));
		}};
		parens = new Language("parens") {{
			rule("S",option(id("S"),symbol('('),id("S"),symbol(')')));
		}};
		mathExpression = new Language("math") {{
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
		grammar = new Language("grammar") {{
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
		ebnf = new Language("ebnf") {{
			Node<String,Void> expression = id("expression");
			rule("syntax", many(id("production")));
			rule("production", id("identifier"), symbol('='), expression, symbol('.'));
			rule(expression, id("term"), many(symbol('|'), id("term")));
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
		cox = new Language("cox") {{
			rule("S", or(list(id("S"), symbol('+'), id("S")), symbol('1')));
		}};
		cox2 = new Language("cox, again") {{
			Node<String,Void> s = id();
			rule(s, or(list(s, symbol('+'), s), symbol('1')));
		}};
		rpn = new Language("rpn") {{
			separator(many(symbol(' ')));
			Node<String,Void> expression = id("expression");
			Node<String,Void> number = id("number");
			rule (expression, or(number, list(expression, expression, oneOf("+-/*"))));
			token (number, many1(range('0','9')));
		}};
		rpn2 = new Language("rpn, again") {{
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
		regex = new Language("regex") {{
			rule("regex",id("term"),many(symbol('|'),id("regex")));
			rule("term",many(id("factor")));
			rule("factor",id("base"), option(symbol('*')));
			rule("base",or(list(option(symbol('\\')), any), list(symbol('('), id("regex"), symbol(')'))));
			//get.debug = true;
		}};
		brainfuck = new Language("brainfuck") {{
			// Program -> Sequence
			rule("Program",id("Sequence"));
			// Sequence -> ( Command | Loop ) *
			rule("Sequence",many(or(id("Command"), id("Loop"))));
			// Command -> '+' | '-' | '<' | '>' | ',' | '.'
			rule("Command",oneOf("+-<>,."));
			// Loop -> '[' Sequence ']'
			rule("Loop",symbol('['), id("Sequence"), symbol(']'));
		}};
		page148 = new Language("page 148") {{
			// S -> A C
			rule("S",id("A"), id("C"));
			// C -> c | epsilon
			rule("C",option(symbol('c')));
			// A -> a B C d | B Q
			rule("A",or(list(symbol('a'), id("B"), id("C"), symbol('d')), list(id("B"), id("Q"))));
			// B -> b B | epsilon
			rule("B",option(symbol('b'), id("B")));
			// Q -> q | epsilon
			rule("Q",option(symbol('q')));
			// get.debug = true;
		}};
		leftRecursion = new Language("left recursion") {{
			rule("L",option(id("L"),symbol('x')));
		}};
		// Regular expressions
		asbs2 = new Language("a*b*") {{
			define(many(symbol('a')),many(symbol('b')));
		}};
		symbol = new Language("symbol") {{
			define(symbol('s'));
		}};
		ab = new Language("ab") {{
			define(list(symbol('a'), symbol('b')));
		}};
		helloWorld = new Language("hello world") {{
			define(string("hello world"));
		}};
		many1any = new Language("(.)+") {{
			define(list(any, many(any)));
		}};
		aaaa = new Language("(a|b)*") {{
			define(many(or(symbol('a'), symbol('b'))));
		}};
		endsWithB = new Language("(.+)b") {{
			define(list(any, many(any), symbol('b')));
		}};
		fooBarFrak = new Language("foo bar frak") {{
			define(many(or(string("foo"),string("bar"),string("frak"))));
		}};
		identifier = new Language("identifier") {{
			// [A-Za-z][A-Za-z0-9]*
			define(or(range('A','Z'), range('a','z')), many(or(range('A','Z'), range('a','z'), range('0', '9'))));
		}};

		languages = new Language[] { 
				asbs, parens, page148, cox,
				cox2, brainfuck, leftRecursion, mathExpression, regex, ebnf, // rpn, rpn2, grammar,
		};
		regularLanguages = new Language[] {
				symbol, ab, helloWorld, many1any, aaaa, endsWithB, fooBarFrak, asbs2,
				identifier
		};
		after = System.nanoTime();
		System.out.format("Setup time: %.2f milliseconds\n", (after - before)/1000000.0);
	}
	public void debug(Language language, String s, boolean matches) {
		language.get.debug = true;
		if (matches) {
			Assert.assertTrue(language.get.matches(s));
		} else {
			Assert.assertFalse(language.get.matches(s));
		}
		language.get.debug = false;
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
		for (int i = 0; i < 100; i++) {
			Assert.assertTrue(mathExpression.get.matches("(((81/08)*4+5*1))/43+28"));
			characters += 23;
			Assert.assertTrue(mathExpression.get.matches("01/(((68-12*18))*37)"));
			characters += 20;
			Assert.assertTrue(mathExpression.get.matches("4*(72+(16*7+50)/2)"));
			characters += 18;
			Assert.assertTrue(mathExpression.get.matches("(8/72)/(43*6+0/8)"));
			characters += 17;
			Assert.assertTrue(mathExpression.get.matches("(0/(7*07+22)-(5))"));
			characters += 17;
			Assert.assertTrue(mathExpression.get.matches("48*((3+43*2)/80)"));
			characters += 16;
			Assert.assertTrue(mathExpression.get.matches("(58*05+34*86)/4"));
			characters += 15;
			Assert.assertTrue(mathExpression.get.matches("05/(38/15)-2*11"));
			characters += 15;
			Assert.assertTrue(mathExpression.get.matches("(0/0-81)*63-5"));
			characters += 13;
			Assert.assertTrue(mathExpression.get.matches("(14-4)*2-4/7"));
			characters += 12;
			Assert.assertTrue(mathExpression.get.matches("1+((5/78+7))"));
			characters += 12;
			Assert.assertTrue(mathExpression.get.matches("(48)-5*6"));
			characters += 8;
			Assert.assertTrue(mathExpression.get.matches("68-50/87"));
			characters += 8;
		}
	}
	
	@Test
	public void testRegexMatching() {
		Assert.assertFalse(symbol.get.nullable.compute());
		Assert.assertFalse(symbol.get.matches("e"));
		Assert.assertTrue(symbol.get.matches("s"));
		Assert.assertTrue(symbol.get.matches("s"));
		Assert.assertFalse(identifier.get.matches("4chan"));
		Assert.assertFalse(identifier.get.matches("2pac"));
		Assert.assertTrue(identifier.get.matches("x"));
		Assert.assertTrue(identifier.get.matches("xyzzy3"));
		Assert.assertTrue(many1any.get.matches("abcdefg"));
		Assert.assertFalse(many1any.get.matches(""));
		Assert.assertFalse(aaaa.get.matches("abcdefg"));
		Assert.assertTrue(aaaa.get.matches("aaaa"));
		Assert.assertTrue(aaaa.get.matches(""));
		Assert.assertTrue(aaaa.get.matches("aabbabab"));
		Assert.assertTrue(ab.get.matches("ab"));
		Assert.assertFalse(ab.get.matches("aaaa"));
		Assert.assertFalse(ab.get.matches(""));
		Assert.assertFalse(ab.get.matches("aabbabab"));
		Assert.assertTrue(endsWithB.get.matches("jebb"));
		Assert.assertFalse(endsWithB.get.matches("jabba"));
		Assert.assertTrue(helloWorld.get.matches("hello world"));
		Assert.assertFalse(helloWorld.get.matches("hello"));
		Assert.assertTrue(fooBarFrak.get.matches("foo"));
		Assert.assertTrue(fooBarFrak.get.matches("foofoobar"));
		Assert.assertFalse(fooBarFrak.get.matches("foobaz"));
		characters += 102;
	}

	@Test
	public void twoPlusTwo() {
		repeat(mathExpression, "2+2", 10000);
	}
	
	@Test
	public void repeatCox() {
		repeat(cox, "1+1+1", 10000);
	}
	
	@Test
	public void repeatStuff() {
		repeat(aaaa, "abba", 10000);
	}
	@Test
	public void repeatStuffAgain() {
		// Rules and identifiers are broken. Regexen are not
		repeat(asbs2, "aaabbb", 10000);
		repeat(asbs, "aaabbb", 10000);
	}

	public void fuzz(Language language, int times) {
		System.out.format("Fuzzing grammar '%s'\n", language.name);
		for (int i = 0; i < times; i++) {
			String s = language.get.generator.compute(10,3).toString();
			characters += s.length();
			boolean result = language.get.matches(s);
			if (!result) {
				// WTF?
				System.out.format("WTF on iteration %d on string %s\n", i, s);
				System.out.println(language.get.gv.compute());
			}
			Assert.assertTrue(result);
		}
	}

	public void repeat(Language language, String s, int times) {
		for (int i = 0; i < times; i++) {
			characters += s.length();
			boolean result = language.get.matches(s);
			if (!result) {
				// WTF?
				System.out.format("WTF for grammar '%s' on iteration %d on string %s\n", language.name, i, s);
				System.out.println(language.get.gv.compute());
			}
			Assert.assertTrue(result);
		}
	}

	@Test
	public void fuzzGrammars() {
		for (Language language : languages) {
			fuzz(language, 100);
		}
	}
	
	@Test
	public void fuzzRegexes() {
		for (Language language : regularLanguages) {
			fuzz(language, 10000);
		}
	}
	
	@Test
	public void fuzzPage148() {
		fuzz(page148, 100000);
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
	public void testCox() {
		Assert.assertTrue(cox.get.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1"));
		characters += 101;
		Assert.assertFalse(cox.get.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1++1"));
		characters += 103;
	}

	@Test
	public void testCox2() {
		long before, after;
		before = System.nanoTime();
		Assert.assertTrue(cox2.get.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1"));
		after = System.nanoTime();
		System.out.println(after - before);
		characters += 101;
		before = System.nanoTime();
		Assert.assertFalse(cox2.get.matches("1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1++1"));
		after = System.nanoTime();
		System.out.println(after - before);
		characters += 103;
	}

	@Test
	public void testMany() {
		Assert.assertTrue(asbs.get.matches("ab"));
		Assert.assertTrue(asbs.get.matches(""));
		Assert.assertTrue(asbs.get.matches("b"));
		Assert.assertFalse(asbs.get.matches("c"));
		Assert.assertTrue(asbs.get.matches("aaaaaabbbb"));
		Assert.assertTrue(asbs.get.matches("aaaaaaaaaaaaaaaaa"));
		Assert.assertFalse(asbs.get.matches("aaaaaaaaabaaaaaaaa"));
		characters += 50;
	}

	@Test
	public void testParens() {
		Assert.assertFalse(parens.get.matches("("));
		Assert.assertTrue(parens.get.matches("()"));
		Assert.assertFalse(parens.get.matches(")"));
		Assert.assertTrue(parens.get.matches(parens.get.firstSet.compute(), "("));
		Assert.assertFalse(parens.get.matches(parens.get.firstSet.compute(), ")"));
		characters += 6;
	}

	@Test
	public void testLeftRecursion() {
		Assert.assertTrue(leftRecursion.get.matches("xxxx"));
		Assert.assertTrue(leftRecursion.get.matches("xx"));
		Assert.assertTrue(leftRecursion.get.matches(""));
		Assert.assertTrue(leftRecursion.get.matches("x"));
		Assert.assertTrue(leftRecursion.get.matches("xxx"));
		Assert.assertTrue(leftRecursion.get.matches("xxxxxxxxxxxxxxxxxxxxxxx"));
		Assert.assertFalse(leftRecursion.get.matches("L"));
		characters += 34;
	}

	@Test
	public void testPage148() {
		//		System.out.println(page148.show(page148.first(page148.id("A"))));
		Assert.assertTrue(page148.get.nullable.compute());
		Assert.assertTrue(page148.get.matches("abd"));
		Assert.assertFalse(page148.get.matches("qcb"));
		Assert.assertFalse(page148.get.matches("adb"));
		debug(page148, "acdc", true);
		Assert.assertFalse(page148.get.matches("acdb"));
		Assert.assertFalse(page148.get.matches("adcb"));
		Assert.assertFalse(page148.get.matches("qb"));
		characters += 23;
	}

	@Test
	public void testBrainfuck() {
		Assert.assertTrue(brainfuck.get.matches("+"));
		Assert.assertTrue(brainfuck.get.matches("++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++."));
		Assert.assertFalse(brainfuck.get.matches("+["));
		Assert.assertFalse(brainfuck.get.matches("+[."));
		Assert.assertFalse(brainfuck.get.matches("+[.+"));
		Assert.assertFalse(brainfuck.get.matches("hi"));
		Assert.assertTrue(brainfuck.get.matches("+[.+]"));
		Assert.assertTrue(brainfuck.get.matches("+[.+]+"));
		Assert.assertFalse(brainfuck.get.matches("boo"));
		characters += 132;
	}

	@Test
	public void testRegexGrammar() {
		Assert.assertTrue(regex.get.matches("a"));
		Assert.assertTrue(regex.get.matches("a|b"));
		Assert.assertTrue(regex.get.matches("a|b**"));
		debug(regex, "(hello)|(world)", true);
		characters += 24;
	}

	@After
	public void summary() {
		// 3528 total
		System.out.format("Allocated %d nodes after matching %d characters\n", Node.allocations, characters);
	}
}
