package to.etc.domui.util;

/**
 * Transforms a given Object into another object, in an unspecified
 * way.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
public interface IValueTransformer<T> {
	public T getValue(Object in) throws Exception;
}
