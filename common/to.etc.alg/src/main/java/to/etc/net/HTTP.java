package to.etc.net;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-07-19.
 */
final public class HTTP {

	private static HttpClient m_client;

	private static List<HttpClient> m_clientList = new ArrayList<>();

	private static Map<SslParameters, HttpClient> m_sslClientMap = new HashMap<>();

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

	public static synchronized HttpClient sslClient(SslParameters ssl) throws Exception {
		HttpClient cl = m_sslClientMap.get(ssl);
		if(null == cl) {
			cl = createSslClient(ssl);
			m_sslClientMap.put(ssl, cl);
			m_clientList.add(cl);
		}
		return cl;
	}

	private static synchronized HttpClient createSslClient(SslParameters ssl) throws Exception {
		byte[] serverThumbprint = ssl.getServerThumbprint();
		SSLContext sslContext;
		if(null != serverThumbprint) {
			sslContext = createSslContextForTrustedServerThumbprint(serverThumbprint);
		}else {
			sslContext = createSscContext(ssl);
		}

		ExecutorService ex = Executors.newCachedThreadPool();

		return HttpClient.newBuilder()
			.executor(ex)
			.sslContext(sslContext)
			.followRedirects(Redirect.NORMAL)
			.version(Version.HTTP_1_1)
			.connectTimeout(Duration.ofMinutes(10))
			.build();
	}

	private static SSLContext createSscContext(SslParameters ssl) throws Exception {
		SslCertificateType sslType = requireNonNull(ssl.getSslType(), "sslType is not set on ssl!");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(sslType.getKeyManagerAlgorithm());
		KeyStore keystore = KeyStore.getInstance(sslType.getKeyStoreType());

		byte[] sslCertificate = requireNonNull(ssl.getSslCertificate(), "sslCertificate is not set on ssl!");
		try(InputStream is = new ByteArrayInputStream(sslCertificate)) {
			String passkey = ssl.getSslPasskey();
			char[] passkeyArray = null != passkey ? passkey.toCharArray() : null;
			keystore.load(is, passkeyArray);
			kmf.init(keystore, passkeyArray);

			SSLContext sslContext = SSLContext.getInstance(sslType.getSslContextProtocol());
			sslContext.init(kmf.getKeyManagers(), null, null);
			return sslContext;
		}
	}

	/**
	 * Creates the ssl context to work against a specific server-side certificate with the specified thumbprint only.
	 */
	private static SSLContext createSslContextForTrustedServerThumbprint(byte[] serverThumbprint) throws Exception {
		X509TrustManager tm = new X509TrustManager() {

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				if (Arrays.stream(chain).noneMatch(crt -> Arrays.equals(crt.getSignature(), serverThumbprint))) {
					throw new CertificateException("Trust chain can not be verified with provided server thumbprint!");
				}
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

	public static void close() {
		List<HttpClient> list;
		synchronized(HTTP.class) {
			list = m_clientList;
			m_clientList = new ArrayList<>();
			HttpClient client = m_client;
			if(null != client) {
				list.add(client);
				m_client = null;
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
}
