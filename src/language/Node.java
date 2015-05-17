package language;

// A regular expression node
public interface Node {
	public <T> T accept(Visitor<T> v);
}
