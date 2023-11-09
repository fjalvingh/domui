package to.etc.net;

import org.eclipse.jdt.annotation.Nullable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class HttpSsl {

	@Nullable
	private static HttpClient m_insecureSslClient;

	private static List<HttpClient> m_clientList = new ArrayList<>();

	private static Map<SslParameters, HttpClient> m_sslClientMap = new HashMap<>();

	public static synchronized HttpClient sslClient(SslParameters ssl) throws Exception {
		HttpClient cl = m_sslClientMap.get(ssl);
		if(null == cl) {
			cl = createSslClient(ssl);
			m_sslClientMap.put(ssl, cl);
			m_clientList.add(cl);
		}
		return cl;
	}

	private static HttpClient createSslClient(SslParameters ssl) throws Exception {
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(ssl.getSslType().getKeyManagerAlgorithm());
		KeyStore keystore = KeyStore.getInstance(ssl.getSslType().getKeyStoreType());

		try(InputStream is = new ByteArrayInputStream(ssl.getSslCertificate())) {
			String passkey = ssl.getSslPasskey();
			char[] passkeyArray = null != passkey ? passkey.toCharArray() : null;
			keystore.load(is, passkeyArray);
			kmf.init(keystore, passkeyArray);

			SSLContext sslContext = SSLContext.getInstance(ssl.getSslType().getSslContextProtocol());
			sslContext.init(kmf.getKeyManagers(), null, null);

			return HttpClient.newBuilder()
				.sslContext(sslContext)
				.followRedirects(Redirect.NORMAL)
				.version(Version.HTTP_1_1)
				.connectTimeout(Duration.ofMinutes(10))
				.build();
		}
	}

	public static synchronized HttpClient insecureSslClient() throws Exception {
		HttpClient insecureSslClient = HttpSsl.m_insecureSslClient;

		if(null != insecureSslClient) {
			return insecureSslClient;
		}

		SSLContext sslContext = insecureContext();

		insecureSslClient = m_insecureSslClient = HttpClient.newBuilder()
				.sslContext(sslContext)
				.followRedirects(Redirect.NORMAL)
				.version(Version.HTTP_1_1)
				.connectTimeout(Duration.ofMinutes(10))
				.build();

		return insecureSslClient;
	}

	private static SSLContext insecureContext() throws Exception {
		X509TrustManager tm = new X509TrustManager() {

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		TrustManager[] noopTrustManager = new TrustManager[1];
		noopTrustManager[0] = tm;
		SSLContext sc = SSLContext.getInstance("ssl");
		sc.init(null, noopTrustManager, null);
		return sc;
	}

	public static void terminate() {
		List<HttpClient> list;
		synchronized(HttpSsl.class) {
			list = m_clientList;
			m_clientList = new ArrayList<>();
			if(null != m_insecureSslClient) {
				list.add(m_insecureSslClient);
				m_insecureSslClient = null;
			}
		}
		for(HttpClient cl : list) {
			try {
				closeClient(cl);
			} catch(Exception x) {
				System.err.println("JdkClient: failed to close " + cl + ": " + x);
			}
		}
	}

	private static void closeClient(HttpClient client) {
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
