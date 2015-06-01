package languageV2.traversal;

import languageV2.Language;

public abstract class AbstractVisitor<T> implements Visitor<T> {
	public final WorkQueue<String> todo;
	public final Language g;
	public AbstractVisitor(Language g, WorkQueue<String> todo) {
		this.todo = todo;
		this.g = g;
	}
	public WorkQueue<String> getWorkList() {
		return todo;
	}
}
