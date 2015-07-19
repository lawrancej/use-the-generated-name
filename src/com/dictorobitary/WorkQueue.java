package com.dictorobitary;

import java.util.Iterator;
import java.util.Set;

public interface WorkQueue<T> extends Iterator<T>, Iterable<T> {

	public void todo(T s);

	public void done(T s);

	public boolean visited(T s);
	
	public Set<T> visited();
	
	public boolean visiting(T s);

	public void clear();

	public boolean hasNext();

	public T next();

	public Iterator<T> iterator();

}