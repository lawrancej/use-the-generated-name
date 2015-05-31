package languageV2;

import util.Pair;
import util.TaggedData;

public class LanguagePair extends Pair<TaggedData<?>,TaggedData<?>> {
	public LanguagePair(TaggedData<?> left, TaggedData<?> right) {
		super(left, right);
	}
}