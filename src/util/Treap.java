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
	
	public boolean has(T key) {
		int comparison = key.compareTo(this.key);
		if (comparison < 0) {
			if (left == null) {
				return false;
			}
			return left.has(key);
		} else if (comparison > 0) {
			if (right == null) {
				return false;
			}
			return right.has(key);
		} else {
			return true;
		}
	}
	
	public static <T extends Comparable<T>> Treap<T> insert(T item, int priority, Treap<T> treap) {
		Treap<T> result = treap;
		if (treap == null) {
			result = create(item, priority);
			return result;
		}
		int comparison = item.compareTo(treap.key);
		if (comparison < 0) {
			treap.left = insert(item, priority, treap.left);
			if (priority(treap.left) > priority(treap)) {
				result = rotateRight(treap);
			}
		} else if (comparison > 0) {
			treap.right = insert(item, priority, treap.right);
			if (priority(treap.right) > priority(treap)) {
				result = rotateLeft(treap);
			}
		}
		return result;
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
	
	private static <T extends Comparable<T>> int height(Treap<T> treap) {
		if (treap == null) return 0;
		return Math.max(height(treap.left) + 1, height(treap.right) + 1);
	}
	
	public int height() {
		return height(this);
	}
	
	private int size(int start, Treap<T> treap) {
		int result = start;
		if (treap == null) return result;
		result ++;
		result = size(result, treap.left);
		result = size(result, treap.right);
		return result;
	}
	
	public int size() {
		return size(0, this);
	}
	
	public static <T extends Comparable<T>> void destructiveSplit(Box<T> less, Box<T> greater, Treap<T> r, T key) {
		r = insert(key, Integer.MAX_VALUE, r);
		less.treap = r.left;
		greater.treap = r.right;
	}
	
	// Replace box with treap?
	public static class Box<T extends Comparable<T>> {
		public Treap<T> treap;
		public static <T extends Comparable<T>> Box<T> create(final Treap<T> t) {
			return new Box<T>() {{ treap = t; }};
		}
	}
	
	public static <T extends Comparable<T>> Treap<T> split(Box<T> less, Box<T> greater, Treap<T> r, T key) {
		if (r == null) {
			less.treap = greater.treap = null;
			return null;
		}
		final Treap<T> root = create(r.key, r.priority);
		int comparison = r.key.compareTo(key);
		if (comparison < 0) {
			less.treap = root;
			return split(new Box<T>() {{treap = root.right;}}, greater, r.right, key);
		} else if (comparison > 0) {
			greater.treap = root;
			return split(less, new Box<T>() {{treap = root.left;}}, r.left, key);
		} else {
			less.treap = r.left;
			greater.treap = r.right;
			return root;
		}
	}
	
	public static <T extends Comparable<T>> Treap<T> join (Treap<T> r1, Treap<T> r2) {
		Treap<T> root;
		if (r1 == null) {
			return r2;
		}
		if (r2 == null) {
			return r1;
		}
		if (r1.priority < r2.priority) {
			root = create(r1.key, r1.priority);
			root.left = r1.left;
			root.right = join(r1.right, r2);
		} else {
			root = create(r2.key, r2.priority);
			root.left = join (r1, r2.left);
			root.right = r1.right;
		}
		return root;
	}
	
	public static <T extends Comparable<T>> Treap<T> union (Treap<T> r1, Treap<T> r2) {
		Treap<T> root;
		Box<T> less, greater;
		less = new Box<T>() {{ treap = null; }};
		greater = new Box<T>() {{ treap = null; }};
		
		if (r1 == null) {
			return r2;
		}
		if (r2 == null) {
			return r1;
		}
		
		if (r1.priority < r2.priority) {
			// FIXME: equivalent to split(&r1, &r2) goes here
		}
		split(less, greater, r2, r1.key);
		
		root = create(r1.key, r1.priority);
		root.left = union(r1.left, less.treap);
		root.right = union(r1.right, greater.treap);
		return root;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		stringify(this, buffer);
		return buffer.toString();
	}
}
