import languageV2.Grammar;


public class GrammarExample {
	public static void main(String[] args) {
		Grammar g = new Grammar() {
			@Override
			public Language<?> language() {
				id("S").derive(id("S"),symbol('('),id("S"),symbol(')'));
				id("S").derive();
				return id("S");
			}
		};
		System.out.println(g);
		System.out.println(g.show(g.first()));
		System.out.println(g.matches("()"));
//		System.out.println(g);
		
//		System.out.println(g.show(g.derivative(')',g.derivative('('))));
	}
}
