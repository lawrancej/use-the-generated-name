package util;

/**
 * Cycle-tolerant singly-linked list.
 *
 * @param <T>
 */
public class Node<T> {
	public T data;
	public Node<T> next;
	
	/**
	 * List cons: add item to the front of the list. O(1)
	 * @param item an item
	 * @param list an existing list
	 * @return the new list
	 */
	public static <T> Node<T> cons(final T item, final Node<T> list) {
		return new Node<T>() {{
			data = item; next = list;
		}};
	}
	
	/**
	 * Find the item in the list. O(n)
	 * @param item the item
	 * @param list the list
	 * @return the containing node
	 */
	public static <T> Node<T> find(final T item, final Node<T> list) {
		Node<T> pointer = list;
		if (pointer == null) {
			return null;
		}
		do {
			if (list.data == item) {
				return pointer;
			}
			pointer = pointer.next;
		} while (pointer != null || pointer != list);
		return null;
	}
	
	/**
	 * Is the list cyclic? O(n)
	 * @param list
	 * @return
	 */
	public static <T> boolean cyclic(final Node<T> list) {
		Node<T> pointer = list;
		if (pointer == null) {
			return false;
		}
		do {
			pointer = pointer.next;
		} while (pointer != null || pointer != list);
		return pointer == list;
	}
	
	/**
	 * Is the item in the list? O(n)
	 * @param item the item
	 * @param list the list
	 * @return if the item is in the list
	 */
	public static <T> boolean in(final T item, final Node<T> list) {
		return find(item, list) != null;
	}
	
	/**
	 * Removes an item in the list. If cyclic, we must ensure the loop is closed. O(n)
	 * @param item
	 * @param list
	 * @return
	 */
	public static <T> Node<T> remove(final T item, final Node<T> list) {
		Node<T> pointer = list;
		if (pointer == null) {
			return null;
		}
		do {
			if (list.data == item) {
				
				return pointer;
			}
			pointer = pointer.next;
		} while (pointer != null || pointer != list);
		return null;
	}
}

