package language;

import java.util.HashMap;
import java.util.Map;

// Regex node representing a character literal
public class Symbol implements Node {
	private static Map<Character, Symbol> instances = new HashMap<Character, Symbol>();
	
	public final char symbol;
	private Symbol(char symbol) {
		this.symbol = symbol;
	}	
	public static Symbol getInstance(char c) {
		if (! instances.containsKey(c)) {
			instances.put(c, new Symbol(c));
		}
		return instances.get(c);
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}

}
