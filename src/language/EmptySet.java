package language;

// Language node representing rejection
public class EmptySet implements Node {
	
	private static final EmptySet instance = new EmptySet();
	private EmptySet() {}
	public static EmptySet getInstance() {
		return instance;
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}

}
