package to.etc.util;

/**
 * Something which behaves as a read-only list.
 *
 * <p>Created on May 26, 2005
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public interface IListable<T> {
	int size() throws Exception;

	T get(int ix) throws Exception;
}
