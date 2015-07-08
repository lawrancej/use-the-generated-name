package util;


public class Node<T> {
	public final int tag;
	public final T data;
	public static int allocations = 0;
	protected Node(int type, T data) {
		this.tag = type;
		this.data = data;
		allocations++;
	}
	// Handy shortcut for the constructor call
	public static <T> Node<T> create(int type, T data) {
		return new Node<T>(type, data);
	}
}