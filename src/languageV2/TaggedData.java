package languageV2;

public class TaggedData<T> extends Data<T> {
	public final int tag;
	public TaggedData(int type, T data) {
		super(data);
		this.tag = type;
	}
}