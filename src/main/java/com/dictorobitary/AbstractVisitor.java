package com.dictorobitary;


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
	public T compute(String id) {
		return compute(g.id(id));
	}
	public T compute() {
		return compute(g.definition());
	}
	@SuppressWarnings("unchecked")
	public T compute(Node<?,?> language) {
		assert language != null;
		getWorkList().clear();
		begin();
		T accumulator;
		// Visit a grammar
		if (Node.tag(language) == Node.Tag.ID) {
			getWorkList().todo((Node<String,Void>)language);
			accumulator = bottom();
			for (Node<String,Void> identifier : getWorkList()) {
				accumulator = reduce(accumulator, g.acceptRule(this, identifier));
				if (done(accumulator)) {
					return end(accumulator);
				}
			}
		}
		// Visit a regex
		else {
			accumulator = Node.accept(this, language);
		}
		return end(accumulator);
	}
}
