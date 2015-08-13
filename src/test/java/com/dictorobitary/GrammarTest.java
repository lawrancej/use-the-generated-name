package com.dictorobitary;

import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
@RunWith(JUnit4.class)
public class GrammarTest {
	static Random rand = new Random();
	static long characters = 0;
	static Language fooBarFrak, helloWorld, aaaa, many1any, ab, asbs, asbs2, repetition, reverse,
	parens, endsWithB, identifier, page148, mathExpression, grammar, ebnf, cox,
	cox2, symbol, rpn, rpn2, regex, brainfuck, leftRecursion, coxOriginal;
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
			int expression = id("expression");
			int term = id("term");
			int factor = id("factor");
			int digit = id("digit");
			int digits = id("digits");
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
			int expression = id("expression");
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
			int s = id();
			rule(s, or(list(s, symbol('+'), s), symbol('1')));
		}};
		rpn = new Language("rpn") {{
//			separator(many(symbol(' ')));
			int expression = id("expression");
			int number = id("number");
			rule (expression, or(number, list(expression, expression, oneOf("+-/*"))));
			token (number, many1(range('0','9')));
		}};
		rpn2 = new Language("rpn, again") {{
//			separator(many(symbol(' ')));
			int expression = id("expression");
			int plus = id("plus");
			int minus = id("minus");
			int div = id("div");
			int times = id("times");
			int number = id("number");
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
		coxOriginal = new Language("cox, original") {{
			rule("S", id("T"));
			rule("T", or(list(id("T"), symbol('+'), id("T")),id("N")));
			rule("N", symbol('1'));
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
		repetition = new Language("repetition") {{
			// aa|bb
			define(or(list(symbol('a'),symbol('a')), list(symbol('b'),symbol('b'))));
		}};
		reverse = new Language("reverse") {{
			// ab|ba
			define(or(list(symbol('a'),symbol('b')), list(symbol('b'),symbol('a'))));
		}};

		languages = new Language[] { 
				asbs, parens, page148, cox,
				cox2,  brainfuck, leftRecursion, mathExpression, regex, ebnf, coxOriginal, // rpn, rpn2, grammar,
		};
		regularLanguages = new Language[] {
				symbol, ab, helloWorld, many1any, aaaa, endsWithB, fooBarFrak, asbs2,
				identifier, repetition, reverse
		};
		after = System.nanoTime();
		System.out.format("Setup time: %.2f milliseconds\n", (after - before)/1000000.0);
	}
	public void test(Language language, String s, boolean expected) {
		characters += s.length();
		boolean result = language.get.matches(s);
		if (expected != result) {
			System.out.format("WTF for %s on string %s\n", language.name, s);
			System.out.println(language.get.gv.compute());
		}
		if (expected) {
			Assert.assertTrue(result);
		} else {
			Assert.assertFalse(result);
		}
	}
	public long timedTest(Language language, String s, boolean expected) {
		long before, after;
		before = System.nanoTime();
		test(language, s, expected);
		after = System.nanoTime();
		return (after - before);
	}
	public void debug(Language language, String s, boolean matches) {
		language.get.debug = true;
		test(language, s, matches);
		language.get.debug = false;
	}
	// Generate a random string
	public String randomString(int length) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < length; i++) {
			buffer.append((char)(rand.nextInt(127)+1));
		}
		return buffer.toString();
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
			test(mathExpression, "(((81/08)*4+5*1))/43+28", true);
			test(mathExpression, "01/(((68-12*18))*37)", true);
			test(mathExpression,"4*(72+(16*7+50)/2)", true);
			test(mathExpression, "(8/72)/(43*6+0/8)", true);
			test(mathExpression, "(0/(7*07+22)-(5))", true);
			test(mathExpression, "48*((3+43*2)/80)", true);
			test(mathExpression, "(58*05+34*86)/4", true);
			test(mathExpression, "05/(38/15)-2*11", true);
			test(mathExpression, "(0/0-81)*63-5", true);
			test(mathExpression, "(14-4)*2-4/7", true);
			test(mathExpression, "1+((5/78+7))", true);
			test(mathExpression, "(48)-5*6", true);
			test(mathExpression, "68-50/87", true);
		}
	}
	@Test
	public void testRegexMatching() {
		Assert.assertFalse(symbol.get.nullable.compute());
		test(symbol, "e", false);
		test(symbol, "s", true);
		test(symbol, "s", true);
		test(identifier, "4chan", false);
		test(identifier, "2pac", false);
		test(identifier, "x", true);
		test(identifier, "xyzzy3", true);
		test(many1any, "abcdefg", true);
		test(many1any, "", false);
		test(aaaa, "abcdefg", false);
		test(aaaa, "aaaa", true);
		test(aaaa, "", true);
		test(aaaa, "aabbabab", true);
		test(ab, "ab", true);
		test(ab, "aaaa", false);
		test(ab, "", false);
		test(ab, "aabbabab", false);
		test(endsWithB, "jebb", true);
		test(endsWithB, "jabba", false);
		test(helloWorld, "hello world", true);
		test(helloWorld, "hello", false);
		test(fooBarFrak, "foo", true);
		test(fooBarFrak, "foofoobar", true);
		test(fooBarFrak, "foobaz", false);
	}
	@Test
	public void twoPlusTwo() {
		repeat(mathExpression, "2+2", 10000);
	}
//	@Test
	public void debugCox() {
		debug(coxOriginal, "1+1", true);
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
		repeat(asbs2, "aaabbb", 10000);
		repeat(asbs, "aaabbb", 10000);
	}
	public void fuzz(Language language, int times) {
		System.out.format("Fuzzing grammar '%s'\n", language.name);
		for (int i = 0; i < times; i++) {
			String s = language.get.generator.compute(10,3).toString();
			repeat(language, s, times);
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
	// We assume that randomly generated strings are not in the language.
	@Test
	public void testRandomStrings() {
		for (Language language : regularLanguages) {
			if (language != many1any && language != endsWithB) {
				for (int i = 0; i < 50; i++) {
					test(language, randomString(50), false);
				}
			}
		}
		for (Language language : languages) {
			if (language != regex) {
				for (int i = 0; i < 50; i++) {
					test(language, randomString(50), false);
				}
			}
		}
	}
	@Test
	public void fuzzGrammars() {
		for (Language language : languages) {
			fuzz(language, 10);
		}
	}
	@Test
	public void fuzzRegexes() {
		for (Language language : regularLanguages) {
			fuzz(language, 100);
		}
	}
	@Test
	public void fuzzPage148() {
		fuzz(page148, 100);
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
		test(cox, "1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1", true);
		test(cox, "1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1++1", false);
	}
	@Test
	public void testCox2() {
		System.out.println(timedTest(cox2, "1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1", true));
		System.out.println(timedTest(cox2, "1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1++1", false));
	}
	@Test
	public void testMany() {
		test(asbs, "ab", true);
		test(asbs, "", true);
		test(asbs, "b", true);
		test(asbs, "c", false);
		test(asbs, "aaaaaabbbb", true);
		test(asbs, "aaaaaaaaaaaaaaaaa", true);
		test(asbs, "aaaaaaaaabaaaaaaaa", false);
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
		test(page148, "abd", true);
		test(page148, "acdc", true);
		test(page148, "acdc", true);
		test(page148, "qcb", false);
		test(page148, "adb", false);
		test(page148, "acdb", false);
		test(page148, "adcb", false);
		test(page148, "qb", false);
	}
	@Test
	public void testBrainfuck() {
		test(brainfuck, "+", true);
		test(brainfuck, "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.", true);
		test(brainfuck, "+[.+]", true);
		test(brainfuck, "+[.+]+", true);
		test(brainfuck, "+[", false);
		test(brainfuck, "+[.", false);
		test(brainfuck, "+[.+", false);
		test(brainfuck, "hi", false);
		test(brainfuck, "boo", false);
	}
	@Test
	public void testRegexGrammar() {
		test(regex, "a", true);
		test(regex, "a|b", true);
		test(regex, "a|b**", true);
		test(regex, "(hello)|(world)", true);
		test(regex,"(a)|(b)", true);
	}
	@Test
	public void testRepetition() {
		test(repetition, "aa", true);
		test(repetition, "bb", true);
		test(repetition, "ab", false);
	}
	@Test
	public void testReverse() {
		test(reverse, "aa", false);
		test(reverse, "bb", false);
		test(reverse,"ab", true);
		test(reverse,"ba", true);
	}
	@After
	public void summary() {
		// 3528 total
//		System.out.format("Allocated %d nodes after matching %d characters\n", allocations, characters);
	}
}
