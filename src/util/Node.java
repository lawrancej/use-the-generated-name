package util;

public class Node<L,R> {
	public final int tag;
	public final L left;
	public final R right;
	public static int allocations = 0;
	protected Node(int type, L left, R right) {
		this.tag = type;
		this.left = left;
		this.right = right;
		allocations++;
	}
	// Handy shortcut for the constructor call
	public static <Left,Right> Node<Left,Right> create(int type, Left left, Right right) {
		return new Node<Left,Right>(type, left, right);
	}
	// Handy shortcut for the constructor call
//	public static <W,X,Y,Z> Node<Node<W,X>,Node<Y,Z>> create(int type, Node<W,X> left, Node<Y,Z> right) {
//		return new Node<Node<W,X>,Node<Y,Z>>(type, left, right);
//	}
}
