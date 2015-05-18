package language;

// Language node base interface (for traversal)
public interface Node {
	public <T> T accept(Visitor<T> v);
}
