package util;


public class TaggedData<T> extends Data<T> {
	public final int tag;
	public TaggedData(int type, T data) {
		super(data);
		this.tag = type;
	}
	// Handy shortcut for the constructor call
	public static <T> TaggedData<T> create(int type, T data) {
		return new TaggedData<T>(type, data);
	}
}