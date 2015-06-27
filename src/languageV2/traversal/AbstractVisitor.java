package languageV2.traversal;

import languageV2.Language;

public abstract class AbstractVisitor<T> implements Visitor<T> {
	public final WorkQueue<Language.Id> todo = new WorkList<Language.Id>();
	public final Language g;
	public AbstractVisitor(Language g) {
		this.g = g;
	}
	public WorkQueue<Language.Id> getWorkList() {
		return todo;
	}
}
