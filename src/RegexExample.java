import static language.Language.*;
import language.Node;
import language.Rule;

public class RegexExample {

	public static void helloTest() {
		Node regex = string("Hello, world!");
		System.out.println(asString(regex));
		System.out.println(matches(regex,"Hello, world!"));
	}

	public static void somethingTest() {
		Node regex2 = many(or(string("foo"),string("bar"),string("frak")));
		System.out.println(asString(regex2));
		System.out.println(matches(regex2, "foofoobar"));
	}
	
	public static void regexTest() {
		let("regex").derive(id("term"),symbol('|'),id("regex"));
		let("regex").derive(id("term"));
		let("term").derive(many(id("factor")));
		let("factor").derive(id("base"), option(symbol('*')));
		let("base").derive(any());
		let("base").derive(symbol('\\'), any());
		let("base").derive(symbol('('), id("regex"), symbol(')'));

//		let("hello").derive(id("hello"), string("world"), id("hello"));
		System.out.println(asString(id("regex")));
//		System.out.println(asString(id("hello")));
	}
	
	public static void leftRecursiveList() {
		let("L").derive(id("L"),symbol('x'));
		let("L").derive();
		System.out.println(asString(id("L")));
		System.out.println(nullable(id("L")));
		System.out.println(matches(id("L"), "x"));
		System.out.println(asString(firstSet(id("L"))));
	}
	
	public static void balancedParens() {
		let("S").derive(id("S"), symbol('('), id("S"), symbol(')'));
		let("S").derive();
		System.out.print("Grammar: ");
		System.out.println(asString(id("S")));
		System.out.print("Nullable? ");
		System.out.println(nullable(id("S")));
		System.out.print("First set: ");
		System.out.println(asString(firstSet(id("S"))));
		System.out.println("Matches ()?");
		System.out.println(matches(id("S"),"()"));
	}
	
	public static void page148() {
		let("S").derive(id("A"), id("C"));
		let("C").derive(symbol('c'));
		let("C").derive();
		let("A").derive(symbol('a'), id("B"), id("C"), symbol('d'));
		let("A").derive(id("B"), id("Q"));
		let("B").derive(symbol('b'), id("B"));
		let("B").derive();
		let("Q").derive(symbol('q'));
		let("Q").derive();
		System.out.println(asString(id("S")));
		System.out.println(asString(firstSet(id("Q"))));
		System.out.println(nullable(id("S")));
	}
	
	public static void other() {
		let("S").derive(id("S"),symbol('e'),id("S"));
		let("S").derive(symbol('a'));
		System.out.println(asString(id("S")));
		System.out.println(asString(firstSet(id("S"))));
		System.out.println(nullable(id("S")));
		System.out.println(matches(id("S"),"aea"));
	}
	
	// Crazy complicated "Hello, world!"
	public static void main(String[] args) {
		balancedParens();
	}
}
