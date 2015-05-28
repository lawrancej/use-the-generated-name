package languageV2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class WorkList<T> implements Iterator<T>, Iterable<T> {
	private Set<T> todo = new HashSet<T>();
	private Set<T> done = new HashSet<T>();
	public void todo(T s) {
		if (!done.contains(s)) {
			todo.add(s);
		}
	}
	public void done(T s) {
		if (!done.contains(s)) {
			done.add(s);
		}
		if (todo.contains(s)) {
			todo.remove(s);
		}
	}
	public boolean visited(T s) {
		return done.contains(s);
	}
	public void clear() {
		todo.clear();
		done.clear();
	}
	@Override
	public boolean hasNext() {
		return todo.iterator().hasNext();
	}
	@Override
	public T next() {
		return todo.iterator().next();
	}
	@Override
	public Iterator<T> iterator() {
		return this;
	}
}