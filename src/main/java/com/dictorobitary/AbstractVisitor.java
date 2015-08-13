package com.dictorobitary;


public abstract class AbstractVisitor<T> implements Visitor<T> {
	public final WorkQueue<Integer> todo = new WorkList<Integer>();
	public final Language g;
	public AbstractVisitor(Language g) {
		this.g = g;
	}
	public WorkQueue<Integer> getWorkList() {
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
	public T compute(int language) {
		getWorkList().clear();
		begin();
		T accumulator;
		// Visit a grammar
		if (g.tag(language) == Language.ID) {
			getWorkList().todo(language);
			accumulator = bottom();
			for (int identifier : getWorkList()) {
				accumulator = reduce(accumulator, g.acceptRule(this, identifier));
				if (done(accumulator)) {
					return end(accumulator);
				}
			}
		}
		// Visit a regex
		else {
			accumulator = g.accept(this, language);
		}
		return end(accumulator);
	}
}
