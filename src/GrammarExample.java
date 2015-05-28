import languageV2.Grammar;


public class GrammarExample {
	public static void main(String[] args) {
		Grammar page148 = new Grammar() {{
			id("S").derives(id("A"), id("C"));
			id("C").derives(symbol('c'));
			id("C").derives();
			id("A").derives(symbol('a'), id("B"), id("C"), symbol('d'));
			id("A").derives(id("B"), id("Q"));
			id("B").derives(symbol('b'), id("B"));
			id("B").derives();
			id("Q").derives(symbol('q'));
			id("Q").derives();
		}};
		System.out.println(page148);
//		System.out.println(page148.show(page148.first(page148.id("A"))));
		System.out.println(page148.nullable());
		
		Grammar regex = new Grammar() {{
			id("regex").derives(id("term"),symbol('|'),id("regex"));
			id("regex").derives(id("term"));
			id("term").derives(many(id("factor")));
			id("factor").derives(id("base"), option(symbol('*')));
			id("base").derives(any);
			id("base").derives(symbol('\\'), any);
			id("base").derives(symbol('('), id("regex"), symbol(')'));
		}};
//		System.out.println(regex);
//		System.out.println(regex.show(regex.first()));
		System.out.println(regex.matches("a|b"));
		System.out.println(regex.matches("a|b**"));
		System.out.println(regex.matches("(hello)|(world)"));
		
//		System.out.println(g);
		
//		System.out.println(g.show(g.derivative(')',g.derivative('('))));
	}
}
