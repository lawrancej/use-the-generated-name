package util;

/**
 * Singly-linked list.
 *
 * @param <T>
 */
public class Node<T> {
	public T data;
	public Node<T> next;
	
	private Node() {}
	
	public static <T> Node<T> create(final T item) {
		return new Node<T>() {{
			data = item; next = null;
		}};
	}
	
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
		while (pointer != null) {
			if (list.data == item) {
				return pointer;
			}
		}
		return pointer;
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
	 * The length of the list
	 * @param list
	 * @return the length
	 */
	public static <T> int length(final Node<T> list) {
		int result = 0;
		Node<T> pointer = list;
		while (pointer != null) {
			result++;
		}
		return result;
	}
}
