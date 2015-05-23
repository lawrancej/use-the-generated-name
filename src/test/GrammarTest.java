package test;

import languageV2.Grammar;

import org.junit.Assert;
import org.junit.Test;

public class GrammarTest {

	@Test
	public void testSymbol() {
		Grammar g = new Grammar() {
			@Override
			public Language<?> language() {
				return symbol('s');
			}
		};
		Assert.assertFalse(g.nullable());
		Assert.assertEquals(Grammar.reject, g.derivative('e'));
		Assert.assertEquals(Grammar.empty, g.derivative('s'));
		Assert.assertTrue(g.matches("s"));
	}

	@Test
	public void testMany() {
		Grammar aaaa = new Grammar() {
			@Override
			public Language<?> language() {
				return list(any, many(any));
			}
		};
		Assert.assertTrue(aaaa.matches("abcdefg"));
		Assert.assertFalse(aaaa.matches(""));
	}

	@Test
	public void testOr() {
		Grammar aaaa = new Grammar() {
			@Override
			public Language<?> language() {
				return many(or(symbol('a'), symbol('b')));
			}
		};
		Assert.assertFalse(aaaa.matches("abcdefg"));
		Assert.assertTrue(aaaa.matches("aaaa"));
		Assert.assertTrue(aaaa.matches(""));
		Assert.assertTrue(aaaa.matches("aabbabab"));
	}

	@Test
	public void testList() {
		Grammar g = new Grammar() {
			@Override
			public Language<?> language() {
				return list(symbol('a'), symbol('b'));
			}
		};
		Assert.assertTrue(g.matches("ab"));
		Assert.assertFalse(g.matches("aaaa"));
		Assert.assertFalse(g.matches(""));
		Assert.assertFalse(g.matches("aabbabab"));
	}
	
	@Test
	public void testAny() {
		Grammar g = new Grammar() {
			@Override
			public Language<?> language() {
				return list(any, many(any), symbol('b'));
			}
		};
		Assert.assertTrue(g.matches("jebb"));
		Assert.assertFalse(g.matches("jabba"));
	}
	
	@Test
	public void testParens() {
		Grammar parens = new Grammar() {
			@Override
			public Language<?> language() {
				id("S").derives(id("S"),symbol('('),id("S"),symbol(')'));
				id("S").derives();
				return id("S");
			}
		};
		Assert.assertTrue(parens.matches(parens.first(), "("));
		Assert.assertFalse(parens.matches(parens.first(), ")"));
		Assert.assertTrue(parens.matches("()"));
		Assert.assertFalse(parens.matches("("));
		Assert.assertFalse(parens.matches(")"));
	}
	
	@Test
	public void testHelloWorld() {
		Grammar g = new Grammar() {
			@Override
			public Language<?> language() {
				return string("hello world");
			}
		};
		Assert.assertTrue(g.matches("hello world"));
		Assert.assertFalse(g.matches("hello"));
	}
	
	@Test
	public void testFooBarFrak() {
		Grammar g = new Grammar() {
			@Override
			public Language<?> language() {
				return many(or(string("foo"),string("bar"),string("frak")));
			}
		};
		Assert.assertTrue(g.matches("foo"));
		Assert.assertTrue(g.matches("foofoobar"));
		Assert.assertFalse(g.matches("foobaz"));
	}
	
	@Test
	public void testLeftRecursion() {
		Grammar g = new Grammar() {
			@Override
			public Language<?> language() {
				id("L").derives(id("L"),symbol('x'));
				id("L").derives();
				return id("L");
			}
		};
//		g.debug = true;
		Assert.assertTrue(g.matches(""));
		Assert.assertTrue(g.matches("x"));
		Assert.assertTrue(g.matches("xx"));
		Assert.assertTrue(g.matches("xxx"));
		Assert.assertFalse(g.matches("L"));
	}
	
	@Test
	public void testRegexGrammar() {
		Grammar regex = new Grammar() {
			@Override
			public Language<?> language() {
				id("regex").derives(id("term"),symbol('|'),id("regex"));
				id("regex").derives(id("term"));
				id("term").derives(many(id("factor")));
				id("factor").derives(id("base"), option(symbol('*')));
				id("base").derives(any);
				id("base").derives(symbol('\\'), any);
				id("base").derives(symbol('('), id("regex"), symbol(')'));
				return id("regex");
			}
		};
		regex.debug = true;
//		System.out.println(regex);
//		System.out.println(regex.show(regex.first()));
		Assert.assertTrue(regex.matches("a"));
//		System.out.println(regex.matches("a|b"));
//		System.out.println(regex.matches("(hello)|(world)"));
	}

/*	@Test
	public void testId() {
		fail("Not yet implemented");
	}

	@Test
	public void testNullable() {
		fail("Not yet implemented");
	}

	@Test
	public void testDerivativeChar() {
		fail("Not yet implemented");
	}

	@Test
	public void testFirst() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatchesString() {
		fail("Not yet implemented");
	}
*/
}
