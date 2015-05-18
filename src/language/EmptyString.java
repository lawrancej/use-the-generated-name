package language;

// Language node representing the empty string
public class EmptyString implements Node {
	private static final EmptyString instance = new EmptyString();
	private EmptyString() {}
	public static EmptyString getInstance() {
		return instance;
	}
	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
