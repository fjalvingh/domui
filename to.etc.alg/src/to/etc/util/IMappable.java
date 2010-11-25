package to.etc.util;

import java.util.*;

/**
 * Something which knows about mapping objects.
 *
 * <p>Created on May 26, 2005
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public interface IMappable<K, V> {
	/**
	 * Looks up an object by key.
	 * @param key
	 * @return
	 */
	public V get(K key);

	/**
	 * Returns the #of objects in the map
	 * @return
	 */
	public int size();

	public Iterator<K> getKeyIterator();

	public Iterator<V> getValueIterator();
}
