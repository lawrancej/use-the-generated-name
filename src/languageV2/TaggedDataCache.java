package languageV2;

import java.util.HashMap;
import java.util.Map;

public class TaggedDataCache<T> {
	private Map<T, TaggedData<T>> instances = new HashMap<T, TaggedData<T>>();
	public TaggedData<T> getInstance(int tag, T key) {
		if (!instances.containsKey(key)) {
			instances.put(key, new TaggedData<T>(tag, key));
		}
		return instances.get(key);
	}
}
