package to.etc.util;

import java.util.*;

/**
 * Something which knows about mapping objects.
 * 
 * <p>Created on May 26, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface Mappable {
	/**
	 * Looks up an object by key.
	 * @param key
	 * @return
	 */
	public Object get(Object key);

	/**
	 * Returns the #of objects in the map
	 * @return
	 */
	public int size();

	public Iterator getKeyIterator();

	public Iterator getValueIterator();
}
