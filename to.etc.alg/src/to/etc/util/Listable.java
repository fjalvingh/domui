package to.etc.util;

/**
 * Something which behaves as a read-only list.
 *
 * <p>Created on May 26, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface Listable {
	public int size() throws Exception;

	public Object get(int ix) throws Exception;
}
