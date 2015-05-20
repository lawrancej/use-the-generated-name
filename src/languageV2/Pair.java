package languageV2;

// A pair of two objects
public class Pair<T1, T2> {
	final public T1 left;
	final public T2 right;
	public Pair(T1 left, T2 right) {
		this.left = left;
		this.right = right;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) return false;
		Pair<T1, T2> other = (Pair<T1, T2>)o;
		return left.equals(other.left) && right.equals(other.right);
	}
	@Override
	public int hashCode() {
		return left.hashCode() ^ right.hashCode();
	}
}
