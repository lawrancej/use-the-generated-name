package test;

import org.junit.Assert;
import org.junit.Test;

import util.Treap;

public class TreapTest {
	
	@Test
	public void testInt() {
		Treap<Integer> treap = null;
		int size = 100;
		for (int i = 0; i < size; i++) {
			treap = Treap.insert(i, treap);
			Assert.assertTrue(treap.has(i));
		}
		// O(log n)
		Assert.assertTrue(treap.height() < 3 * (Math.log(size) / Math.log(2)));
		Assert.assertTrue(treap.size() == size);
		Assert.assertTrue(treap.has(0));
		Assert.assertFalse(treap.has(size));
		Treap.Box<Integer> less = Treap.Box.create(null);
		Treap.Box<Integer> greater = Treap.Box.create(null);
		Treap.destructiveSplit(less, greater, treap, 50);
		System.out.println(less.treap);
		System.out.println(greater.treap);
	}

	@Test
	public void testString() {
		Treap<String> treap = null;
		int size = 100000;
		for (int i = 0; i < size; i++) {
			String element = "" + i;
			treap = Treap.insert(element, treap);
			Assert.assertTrue(treap.has(element));
		}
		// O(log n)
		Assert.assertTrue(treap.height() < 3 * (Math.log(size) / Math.log(2)));
		Assert.assertTrue(treap.size() == size);
		Assert.assertFalse(treap.has("" + size));
	}

}
