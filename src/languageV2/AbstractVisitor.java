package languageV2;

public abstract class AbstractVisitor<T> implements Visitor<T> {
	public final WorkList<String> todo;
	public final Grammar g;
	public AbstractVisitor(Grammar g, WorkList<String> todo) {
		this.todo = todo;
		this.g = g;
	}
	public WorkList<String> getWorkList() {
		return todo;
	}
}
