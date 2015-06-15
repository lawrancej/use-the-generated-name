package test;

import org.junit.Test;

import util.Treap;

public class TreapTest {
	
	@Test
	public void testInt() {
		Treap<String> treap = null;
		for (int i = 0; i < 1000000; i++) {
			treap = Treap.insert("" + i, treap);
		}
//		System.out.println(treap);
		System.out.println(Treap.height(treap));
		System.out.println(Treap.size(0, treap));
	}

	@Test
	public void test() {
		Treap<String> hey = Treap.create("hello");
		System.out.println(hey);
		hey = Treap.insert("welcome", hey);
		System.out.println(hey);
		hey = Treap.insert("howdy", hey);
		System.out.println(hey);
		hey = Treap.insert("greetings", hey);
		System.out.println(hey);
		hey = Treap.insert("aloha", hey);
		System.out.println(hey);
		hey = Treap.insert("mahalo", hey);
		System.out.println(hey);
		hey = Treap.insert("wow", hey);
		System.out.println(hey);
		hey = Treap.insert("good to see you", hey);
		System.out.println(hey);
		hey = Treap.insert("hola", hey);
		System.out.println(hey);
		hey = Treap.insert("hi", hey);
		System.out.println(hey);
		hey = Treap.insert("yo", hey);
		System.out.println(hey);
		hey = Treap.insert("sup", hey);
		System.out.println(hey);
		hey = Treap.insert("derp", hey);
		System.out.println(hey);
		hey = Treap.insert("wat", hey);
		System.out.println(hey);
		hey = Treap.insert("omg", hey);
		System.out.println(hey);
		hey = Treap.insert("hey", hey);
		System.out.println(hey);
		hey = Treap.insert("doom", hey);
		System.out.println(hey);
		hey = Treap.insert("boss", hey);
		System.out.println(hey);
		hey = Treap.insert("drat", hey);
		System.out.println(hey);
		hey = Treap.insert("why", hey);
		System.out.println(hey);
		hey = Treap.insert("because", hey);
		System.out.println(hey);
		hey = Treap.insert("do", hey);
		System.out.println(hey);
		hey = Treap.insert("or do not", hey);
		System.out.println(hey);
		hey = Treap.insert("there is", hey);
		System.out.println(hey);
		hey = Treap.insert("no", hey);
		System.out.println(hey);
		hey = Treap.insert("try", hey);
		System.out.println(hey);
		hey = Treap.insert("catch", hey);
		System.out.println(hey);
		hey = Treap.insert("block", hey);
		System.out.println(hey);
		hey = Treap.insert("for", hey);
		System.out.println(hey);
		hey = Treap.insert("you", hey);
		System.out.println(hey);
		hey = Treap.insert("too", hey);
		System.out.println(hey);
		System.out.println(Treap.height(hey));
		System.out.println(Treap.size(0, hey));
	}

}
