package languageV2;

public abstract class AbstractVisitor<T> implements Visitor<T> {
	public final WorkQueue<String> todo;
	public final Grammar g;
	public AbstractVisitor(Grammar g, WorkQueue<String> todo) {
		this.todo = todo;
		this.g = g;
	}
	public WorkQueue<String> getWorkList() {
		return todo;
	}
}
