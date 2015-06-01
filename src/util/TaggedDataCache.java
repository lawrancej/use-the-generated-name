package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
	public void clear() {
		instances.clear();
	}
	public Collection<TaggedData<T>> values() {
		return instances.values();
	}
	public int size() {
		return instances.size();
	}
	public Set<T> keySet() {
		return instances.keySet();
	}
}
