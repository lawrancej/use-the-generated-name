package languageV2.traversal;

import languageV2.Language;
import languageV2.Node;

public abstract class AbstractVisitor<T> implements Visitor<T> {
	public final WorkQueue<Node<String,Void>> todo = new WorkList<Node<String,Void>>();
	public final Language g;
	public AbstractVisitor(Language g) {
		this.g = g;
	}
	public WorkQueue<Node<String,Void>> getWorkList() {
		return todo;
	}
	public void begin() {}
	public T end(T accumulator) {
		return accumulator;
	}
	public T compute() {
		return g.beginTraversal(this);
	}
	public T compute(Node<?,?> language) {
		return g.beginTraversal(this, language);
	}
}
