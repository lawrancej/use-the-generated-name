import static language.Language.*;
import language.Node;

public class RegexExample {

	// Crazy complicated "Hello, world!"
	public static void main(String[] args) {
		Node regex = string("Hello, world!");
		System.out.println(asString(regex));
		System.out.println(matches(regex,"Hello, world!"));
		
		Node regex2 = many(or(string("foo"),string("bar"),string("frak")));
		System.out.println(asString(regex2));
		System.out.println(matches(regex2, "foofoobar"));
		
		let("regex").derive(id("term"),symbol('|'),id("regex"));
		let("regex").derive(id("term"));
		let("term").derive(many(id("factor")));
		let("factor").derive(id("base"), option(symbol('*')));
		let("base").derive(any());
		let("base").derive(symbol('\\'), any());
		let("base").derive(symbol('('), id("regex"), symbol(')'));

		let("hello").derive(id("hello"), string("world"), id("hello"));
		System.out.println(asString(id("regex")));
		System.out.println(asString(id("hello")));
	}
}
