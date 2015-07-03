package util;


public class TaggedDataPair extends Pair<TaggedData<?>,TaggedData<?>> {
	public TaggedDataPair(TaggedData<?> left, TaggedData<?> right) {
		super(left, right);
	}
}