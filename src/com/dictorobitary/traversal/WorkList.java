package com.dictorobitary.traversal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class WorkList<T> implements WorkQueue<T> {
	private Set<T> todo = new HashSet<T>();
	private Set<T> done = new HashSet<T>();
	private T current;
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
	public boolean hasNext() {
		return todo.iterator().hasNext();
	}
	public T next() {
		current = todo.iterator().next();
		return current;
	}
	public Iterator<T> iterator() {
		return this;
	}
	public Set<T> visited() {
		return done;
	}
	public boolean visiting(T s) {
		return s == current;
	}
}