package test;

import static org.junit.Assert.*;
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
		fail("Not yet implemented");
	}

	@Test
	public void testOr() {
		fail("Not yet implemented");
	}

	@Test
	public void testList() {
		fail("Not yet implemented");
	}

	@Test
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

}
