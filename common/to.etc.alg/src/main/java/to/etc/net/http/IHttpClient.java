package to.etc.net.http;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-03-22.
 */
public interface IHttpClient {
	<T> GenericHttpResponse<T> send(GenericHttpRequest request, IBodyReader<T> reader) throws Exception;

	void close();
}
