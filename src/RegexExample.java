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
		
		let("regex").derive(nonterm("term"),symbol('|'),nonterm("regex"));
		let("regex").derive(nonterm("term"));
		let("term").derive(many(nonterm("factor")));
		let("factor").derive(nonterm("base"), option(symbol('*')));
		let("base").derive(symbol('.'));
		let("base").derive(symbol('\\'), symbol('.'));
		let("base").derive(symbol('('), nonterm("regex"), symbol(')'));

//		let("hello").derive(nonterm("hello"), string("world"), nonterm("hello"));
		System.out.println(asString(nonterm("regex")));
	}
}
