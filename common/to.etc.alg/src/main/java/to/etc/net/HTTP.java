package to.etc.net;

import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.Nonnull;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-07-19.
 */
final public class HTTP {
	private static HttpClient m_client;

	private HTTP() {}

	static public synchronized HttpClient client() {
		HttpClient client = m_client;
		if(null == client) {
			ExecutorService ex = Executors.newCachedThreadPool();

			m_client = client = HttpClient.newBuilder()
				.executor(ex)
				.followRedirects(Redirect.NORMAL)
				.version(Version.HTTP_1_1)
				.build();
		}
		return client;
	}

	@Nonnull
	static public HttpClient sslClient(@Nonnull byte[] certificate, @Nonnull SslCertificateType type, @Nullable String passkey) throws Exception {

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(type.getKeyManagerAlgorithm());
		KeyStore keystore = KeyStore.getInstance(type.getKeyStoreType());

		try(InputStream is = new ByteArrayInputStream(certificate)) {
			char[] passkeyArray = null != passkey ? passkey.toCharArray() : null;
			keystore.load(is, passkeyArray);
			kmf.init(keystore, passkeyArray);

			SSLContext sslContext = SSLContext.getInstance(type.getSslContextProtocol());
			sslContext.init(kmf.getKeyManagers(), null, null);

			return HttpClient.newBuilder()
				.sslContext(sslContext)
				.followRedirects(Redirect.NORMAL)
				.version(Version.HTTP_1_1)
				.connectTimeout(Duration.ofMinutes(10))
				.build();
		}
	}

	static public synchronized void close() {
		HttpClient client = m_client;
		if(null != client) {
			m_client = null;
			System.out.println("destroy: closing http client resources");
			Optional<Executor> executorStupidity = client.executor();
			if(executorStupidity.isPresent()) {					// Sure. This is of course better than NULL because no one would forget this. Idiots. And now the compiler cannot check.
				System.out.println("destroy: preparing to close HTTP executor");
				Executor executor = executorStupidity.get();
				if(executor instanceof ExecutorService) {
					((ExecutorService) executor).shutdownNow();
					System.out.println("destroy: closed HTTP executor");
				}
			}
		}
	}
}
