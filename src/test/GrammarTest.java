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
		Grammar aaaa = new Grammar() {
			@Override
			public Language<?> language() {
				return list(symbol('a'), symbol('b'));
			}
		};
		Assert.assertTrue(aaaa.matches("ab"));
		Assert.assertFalse(aaaa.matches("aaaa"));
		Assert.assertFalse(aaaa.matches(""));
		Assert.assertFalse(aaaa.matches("aabbabab"));
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
