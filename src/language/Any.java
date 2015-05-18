package language;

// Match any character
public class Any implements Node {
	private static final Any instance = new Any();
	private Any() {}
	public static Any getInstance() {
		return instance;
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}

}
