package languageV2;

import java.util.HashMap;
import java.util.Map;


public class TaggedDataCache<T> {
	/* Map the data to the instance */
	private Map<T, TaggedData<T>> instances = new HashMap<T, TaggedData<T>>();
	private final int tag;
	private final TaggedData<T> bottom;
	public TaggedDataCache(TaggedData<T> bottom) {
		this.tag = bottom.tag;
		this.bottom = bottom;
	}
	public TaggedData<T> getInstance(T key) {
		if (key == null) return bottom;
		if (!instances.containsKey(key)) {
			instances.put(key, new TaggedData<T>(tag, key));
		}
		return instances.get(key);
	}
}
