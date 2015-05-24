package languageV2;

public class Node<T> extends Data<T> {
	public final Node<T> next;
	Node(T data, Node<T> next) {
		super(data);
		this.next = next;
	}
	Node(T data) {
		this(data, null);
	}
	@Override
	public int hashCode() {
		if (next == null) {
			return data.hashCode();
		}
		return data.hashCode() ^ next.hashCode();
	}
/*
	@Override
	public boolean equals(Object that) {
		if (that instanceof Node<?>) {
		}
		return false;
	}
*/
}

