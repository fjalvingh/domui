package to.etc.net.http;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-03-22.
 */
public interface IBodyReader<T> {
	Class<T> getTypeClass();
}
