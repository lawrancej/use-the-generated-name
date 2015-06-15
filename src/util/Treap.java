package util;

// A treap implementation
public class Treap<T extends Comparable<T>> {
	public final int priority;
	public final T key;
	public Treap<T> left;
	public Treap<T> right;
	
	public static <T extends Comparable<T>> int priority(Treap<T> treap) {
		if (treap == null) {
			return Integer.MIN_VALUE;
		}
		return treap.priority;
	}
	
	private Treap(T key, int priority, Treap<T> left, Treap<T> right) {
		this.priority = priority;
		this.key = key;
		this.left = left;
		this.right = right;
	}
	private Treap(T key, Treap<T> left, Treap<T> right) {
		this.priority = this.hashCode();
		this.key = key;
		this.left = left;
		this.right = right;
	}
	
	public static <T extends Comparable<T>> Treap<T> create(T key) {
		return new Treap<T>(key, null, null);
	}
	
	public static <T extends Comparable<T>> Treap<T> create(T key, int priority) {
		return new Treap<T>(key, priority, null, null);
	}
	
	public static <T extends Comparable<T>> boolean in(Treap<T> treap, T key) {
		if (treap == null) {
			return false;
		}
		int comparison = key.compareTo(treap.key);
		if (comparison < 0) {
			return in(treap.left, key);
		} else if (comparison > 0) {
			return in(treap.right, key);
		} else {
			return true;
		}
	}
	
	public static <T extends Comparable<T>> Treap<T> insert(T item, Treap<T> treap) {
		Treap<T> result = treap;
		if (treap == null) {
			result = create(item);
			return result;
		}
		int comparison = item.compareTo(treap.key);
		if (comparison < 0) {
			treap.left = insert(item, treap.left);
			if (priority(treap.left) > priority(treap)) {
				result = rotateRight(treap);
			}
		} else if (comparison > 0) {
			treap.right = insert(item, treap.right);
			if (priority(treap.right) > priority(treap)) {
				result = rotateLeft(treap);
			}
		}
		return result;
	}
	
	public static <T extends Comparable<T>> Treap<T> rotateLeft(Treap<T> treap) {
		Treap<T> pivot = treap.right;
		treap.right = pivot.left;
		pivot.left = treap;
		treap = pivot;
		return treap;
	}
	public static <T extends Comparable<T>> Treap<T> rotateRight(Treap<T> treap) {
		Treap<T> pivot = treap.left;
		treap.left = pivot.right;
		pivot.right = treap;
		treap = pivot;
		return treap;
	}
	
	public static <T extends Comparable<T>> StringBuffer stringify(Treap<T> treap, StringBuffer buffer) {
		if (treap != null) {
			buffer.append("(");
			buffer.append(treap.key);
			if (treap.left == null && treap.right == null) {
				buffer.append(")");
				return buffer;
			}
			buffer.append(" ");
			stringify(treap.left,buffer);
			buffer.append(" ");
			stringify(treap.right,buffer);
			buffer.append(")");
		}
		else {
			buffer.append("null");
		}
		return buffer;
	}
	
	public static <T extends Comparable<T>> int height(Treap<T> treap) {
		if (treap == null) return 0;
		return Math.max(height(treap.left) + 1, height(treap.right) + 1);
	}
	
	public static <T extends Comparable<T>> int size(int start, Treap<T> treap) {
		int result = start;
		if (treap == null) return result;
		result ++;
		result = size(result, treap.left);
		result = size(result, treap.right);
		return result;
	}
	
	/*
	public static <T extends Comparable<T>> Treap<T> split(Treap<T> less, Treap<T> greater, Treap<T> r, T key) {
		Treap<T> root;
		// this is broken, btw, because less and greater are pass by value reference, not by reference
		if (r == null) {
			less = greater = null;
			return null;
		}
		root = create(key, r.priority);
		int comparison = r.key.compareTo(key);
		if (comparison == 0) {
			less = r.left;
			greater = r.right;
			return root;
		} else if (comparison > 0) {
			
		} else if (comparison < 0) {
			
		}
	}
	*/
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		stringify(this, buffer);
		return buffer.toString();
	}
}
