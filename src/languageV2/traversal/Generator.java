package languageV2.traversal;

import java.util.Random;

import languageV2.Language;
import languageV2.Node;


/**
 * Randomly generate strings in a language.
 * 
 * @author Joey Lawrance
 *
 */
public class Generator extends AbstractVisitor<StringBuffer> {

	Random rand = new Random();
	StringBuffer buffer;
	public Generator(Language g) {
		super(g);
	}

	@Override
	public StringBuffer symbol(Node<Character, Character> language) {
		if (language == Language.any) {
			return buffer.append((char)(rand.nextInt(127)+1));
		} else if (language.left == language.right) {
			return buffer.append(language.left);
		} else {
			return buffer.append((char)(rand.nextInt(language.right - language.left) + language.left));
		}
	}

	@Override
	public StringBuffer list(Node<Node<?, ?>, Node<?, ?>> language) {
		g.accept(this, language.left);
		g.accept(this, language.right);
		return buffer;
	}

	@Override
	public StringBuffer set(Node<Node<?, ?>, Node<?, ?>> set) {
		if (rand.nextInt(2) == 0) {
			g.accept(this, set.left);
		} else {
			g.accept(this, set.right);
		}
		return buffer;
	}

	@Override
	public StringBuffer id(Node<String, Void> id) {
		g.acceptRule(this, id);
		return buffer;
	}

	@Override
	public StringBuffer rule(Node<Node<String, Void>, Node<?, ?>> rule) {
		g.accept(this, rule.right);
		return buffer;
	}

	@Override
	public StringBuffer bottom() {
		return buffer;
	}

	@Override
	public boolean done(StringBuffer accumulator) {
		return false;
	}

	@Override
	public StringBuffer reduce(StringBuffer accumulator, StringBuffer current) {
		return buffer;
	}
	
	@Override
	public void begin() {
		buffer = new StringBuffer();
	}

}
