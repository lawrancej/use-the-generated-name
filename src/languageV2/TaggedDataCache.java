package languageV2;

import java.util.HashMap;
import java.util.Map;

/* Caches instances of data so that value comparisons become pointer comparisons */
public class TaggedDataCache<T> {
	/* Map the data to the instance */
	private Map<T, TaggedData<T>> instances = new HashMap<T, TaggedData<T>>();
	private final int tag;
	private final TaggedData<T> bottom;
	private TaggedDataCache(TaggedData<T> bottom) {
		this.tag = bottom.tag;
		this.bottom = bottom;
	}
	public static <T> TaggedDataCache<T> create(TaggedData<T> bottom) {
		return new TaggedDataCache<T>(bottom);
	}
	public TaggedData<T> getInstance(T key) {
		if (key == null) return bottom;
		if (!instances.containsKey(key)) {
			instances.put(key, TaggedData.create(tag, key));
		}
		return instances.get(key);
	}
}
