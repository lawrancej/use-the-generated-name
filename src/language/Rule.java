package language;

public class Rule implements Node {

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}

}
