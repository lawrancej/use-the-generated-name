package util;


public class TaggedData<T> {
	public final int tag;
	public final T data;
	public static int allocations = 0;
	protected TaggedData(int type, T data) {
		this.tag = type;
		this.data = data;
		allocations++;
	}
	// Handy shortcut for the constructor call
	public static <T> TaggedData<T> create(int type, T data) {
		return new TaggedData<T>(type, data);
	}
}