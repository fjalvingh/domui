package to.etc.domui.util;

/**
 * Specifies a way in which an object instance can be converted to an unique key representation
 * of that object (usually by just rendering the primary key as a string), and the reverse.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 24, 2008
 */
public interface IKeyTranslator<T> {
	public String		getRenderableKey(T source);
	public T			getObjectByKey(String key) throws Exception;
}
