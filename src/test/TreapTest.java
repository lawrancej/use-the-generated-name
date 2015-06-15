package test;

import org.junit.Test;

import util.Treap;

public class TreapTest {
	
	@Test
	public void testInt() {
		Treap<Integer> treap = null;
		for (int i = 0; i < 1000000; i++) {
			treap = Treap.insert(i, treap);
		}
//		System.out.println(treap);
		System.out.println(Treap.height(treap));
		System.out.println(Treap.size(0, treap));
		System.out.println(Treap.in(treap, 999));
		System.out.println(Treap.in(treap, 1000));
		System.out.println(Treap.in(treap, 1000000));
	}

	@Test
	public void test() {
		Treap<String> treap = null;
		for (int i = 0; i < 1000000; i++) {
			treap = Treap.insert("" + i, treap);
		}
//		System.out.println(treap);
		System.out.println(Treap.height(treap));
		System.out.println(Treap.size(0, treap));
		System.out.println(Treap.in(treap, "999"));
		System.out.println(Treap.in(treap, "1000"));
		System.out.println(Treap.in(treap, "1000000"));
	}

}
