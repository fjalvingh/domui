package to.etc.net.http;

import to.etc.net.http.apacheimpl.ApacheHttpClient;
import to.etc.net.http.jdkimpl.JdkHttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Accessor to all generic client implementations.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-03-22.
 */
final public class GenericHttpClient {
	static private Map<HttpImpl, IHttpClient> m_implMap = new HashMap<>();

	static private volatile HttpImpl m_defaultImpl = HttpImpl.JDK;

	static private final IHttpClient m_implWrapper = new GenericClientImpl();

	/**
	 * Override the default http client implementation globally.
	 */
	public static void setDefaultImpl(HttpImpl defaultImpl) {
		m_defaultImpl = defaultImpl;
	}

	static private IHttpClient clientFor(GenericHttpRequest r) {
		HttpImpl ci = r.getClientImpl();
		if(ci == null)
			ci = m_defaultImpl;
		if(ci == HttpImpl.DEFAULT)
			ci = m_defaultImpl;
		IHttpClient impl = m_implMap.get(ci);
		if(null == impl)
			throw new IllegalStateException("No HTTP client implementation '" + ci + "'");
		return impl;
	}

	static public IHttpClient defaultClient() {
		return m_implWrapper;
	}

	//public static IHttpClient defaultSslClient(byte[] certificate, SslCertificateType type, String passkey) {
	//	throw new IllegalStateException("Impl");
	//}

	static public void terminate() {
		for(IHttpClient client : m_implMap.values()) {
			try {
				client.close();
			} catch(Exception x) {
				System.err.println("HttpClient: failed to terminate " + client + ": " + x);
			}
		}
	}

	static {
		m_implMap.put(HttpImpl.JDK, new JdkHttpClient());
		m_implMap.put(HttpImpl.APACHE, new ApacheHttpClient());
	}

	static private final class GenericClientImpl implements IHttpClient {
		@Override
		public <T> GenericHttpResponse<T> send(GenericHttpRequest request, IBodyReader<T> reader) throws Exception {
			return clientFor(request).send(request, reader);
		}

		@Override
		public void close() {
		}
	}


}
