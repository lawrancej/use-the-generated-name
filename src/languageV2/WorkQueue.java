package languageV2;

import java.util.Iterator;

interface WorkQueue<T> extends Iterator<T>, Iterable<T> {

	public abstract void todo(T s);

	public abstract void done(T s);

	public abstract boolean visited(T s);

	public abstract void clear();

	public abstract boolean hasNext();

	public abstract T next();

	public abstract Iterator<T> iterator();

}