package to.etc.net.http.jdkimpl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.alg.process.NamedThreadFactory;
import to.etc.net.http.BodyProducers.EmptyBodyProducer;
import to.etc.net.http.BodyProducers.StringBodyProducer;
import to.etc.net.http.GenericHttpHeaders;
import to.etc.net.http.GenericHttpRequest;
import to.etc.net.http.GenericHttpResponse;
import to.etc.net.http.IBodyReader;
import to.etc.net.http.IHttpBodyProducer;
import to.etc.net.http.IHttpClient;
import to.etc.net.http.SslCertificateType;
import to.etc.net.http.SslParameters;
import to.etc.util.WrappedException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
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
import static to.etc.util.SecurityUtils.getSha1Thumbprint;

/**
 * Implements the generic HTTP client layer using the JDK's
 * HttpClient impl. This should be the default.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-03-22.
 */
public class JdkHttpClient implements IHttpClient {
	@Nullable
	private HttpClient m_client;

	private List<HttpClient> m_clientList = new ArrayList<>();

	private Map<SslParameters, HttpClient> m_sslClientMap = new HashMap<>();

	/**
	 * Instance that is uses as global scope JDK HTTP client provider.
	 * It has to be closed at the end of application life to properly close all http clients cached meanwhile.
	 */
	public static final JdkHttpClient HTTP = new JdkHttpClient();

	private JdkHttpClient() {

	}

	@Override
	public <T> GenericHttpResponse<T> send(GenericHttpRequest request, IBodyReader<T> reader) throws Exception {
		Builder b = HttpRequest.newBuilder()
			.uri(new URI(request.getUrl()));

		String method = request.getMethod();
		if(null == method) {
			method = "GET";
		}
		switch(method.toUpperCase()) {
			case "GET":
				//-- Just issue a GET without any BODY
				b.GET();
				break;

			default:
				b.method(method.toUpperCase(), calculateBody(request));
				break;
		}

		request.getHeaderMap().forEach((name, value) -> {
			for(String s : value) {
				b.header(name, s);
			}
		});
		Duration timeout = request.getTimeout();
		if(null != timeout) {
			/*
			 * This is rather useless as this only defines a timeout between the connection
			 * succeeding and the headers being received. There is no way to define a socket
			 * timeout, unbelievable enough, apparently there is an exchange of incompetents
			 * between MS and whomever built this -(. We've been doing socket connections
			 * since 1980, hard to believe this kind of mistake is still made.
			 *
			 * In effect this means that the java implementation should not be used for
			 * anything MS like.
			 *
			 * See https://stackoverflow.com/questions/64550136/how-to-set-socket-timeout-in-java-http-client
			 * and https://bugs.openjdk.org/browse/JDK-8258397
			 */
			b.timeout(timeout);
		}

		HttpRequest hr = b.build();

		BodyHandler<T> handler = handlerFromReader(reader);
		HttpResponse<T> response = client(request).send(hr, handler);

		GenericHttpHeaders gh = new GenericHttpHeaders(response.headers().map());
		return new GenericHttpResponse<>(response.statusCode(), gh, response.body());
	}

	private <T> BodyHandler<T> handlerFromReader(IBodyReader<T> reader) {
		if(reader.getTypeClass() == String.class)
			return (BodyHandler<T>) BodyHandlers.ofString();
		else if(reader.getTypeClass() == InputStream.class)
			return (BodyHandler<T>) BodyHandlers.ofInputStream();
		else
			throw new IllegalStateException("Unsupported BodyReader " + reader.getTypeClass().getName());
	}

	private BodyPublisher calculateBody(GenericHttpRequest request) {
		IHttpBodyProducer body = request.getBody();
		if(body instanceof EmptyBodyProducer || body == null) {
			return BodyPublishers.noBody();
		} else if(body instanceof StringBodyProducer sbp) {
			return BodyPublishers.ofString((sbp).getData());
		} else {
			throw new IllegalStateException("Unsupported body producer: " + body.getClass().getName());
		}
	}

	private HttpClient client(GenericHttpRequest r) throws Exception {
		SslParameters ssl = r.getSslParameters();
		if(null != ssl)
			return sslClient(ssl);
		else
			return client();
	}

	public synchronized HttpClient sslClient(SslParameters ssl) throws Exception {
		HttpClient cl = m_sslClientMap.get(ssl);
		if(null == cl) {
			cl = createSslClient(ssl);
			m_sslClientMap.put(ssl, cl);
			m_clientList.add(cl);
		}
		return cl;
	}

	private HttpClient createSslClient(SslParameters ssl) throws Exception {
		SSLContext sslContext = createSSLContext(ssl);
		ExecutorService ex = Executors.newCachedThreadPool(new NamedThreadFactory("jdkSslClnt"));

		return HttpClient.newBuilder()
			.executor(ex)
			.sslContext(sslContext)
			.followRedirects(Redirect.NORMAL)
			.version(Version.HTTP_1_1)
			.connectTimeout(Duration.ofMinutes(10))
			.build();
	}

	static public SSLContext createSSLContext(SslParameters ssl) throws Exception {
		byte[] certSha1Thumbprint = ssl.getCertSha1Thumbprint();

		TrustManager tm;
		if(certSha1Thumbprint != null) {
			tm = createX509TrustManagerForCert(certSha1Thumbprint);
		} else if(ssl.isIgnoreRemoteCertificate()) {
			tm = createTrustManagerTrustAll();
		} else {
			tm = null;
		}

		SSLContext sslContext = createSSLContext(ssl, tm);
		return sslContext;
	}

	static private SSLContext createSSLContext(SslParameters parameters, @Nullable TrustManager tm) throws Exception {
		if(parameters.getSslType() == null) {
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, tm == null ? null : new TrustManager[] { tm }, null);
			return sc;
		} else {
			return createSslContextWithClientKey(parameters, tm);
		}
	}

	static private SSLContext createSslContextWithClientKey(SslParameters ssl, @Nullable TrustManager tm) throws Exception {
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
			sslContext.init(kmf.getKeyManagers(), tm == null ? null : new TrustManager[] { tm }, null);
			return sslContext;
		}
	}

	/**
	 * Create a trust manager which accepts all server certificates without checking.
	 */
	@NonNull
	private static TrustManager createTrustManagerTrustAll() {
		TrustManager trustAllCerts = new X509ExtendedTrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			@SuppressWarnings("squid:S4830")
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			@SuppressWarnings("squid:S4830")
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			@SuppressWarnings("squid:S4830")
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
			}

			@SuppressWarnings("squid:S4830")
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
			}

			@SuppressWarnings("squid:S4830")
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
			}

			@SuppressWarnings("squid:S4830")
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
			}
		};
		return trustAllCerts;
	}

	/**
	 * Create a trust manager which only accepts a server with a specific
	 * certificate.
	 */
	@Nullable
	private static X509TrustManager createX509TrustManagerForCert(byte[] certSha1Thumbprint) throws Exception {
		X509TrustManager tm = new X509TrustManager() {
			@Override
			@SuppressWarnings("squid:S4830")
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				if(Arrays.equals(SslParameters.INSECURE_SSL_THUMBPRINT.getBytes(StandardCharsets.UTF_8), certSha1Thumbprint)) {
					return;
				}
				if(Arrays.stream(chain).noneMatch(crt -> {
					try {
						return Arrays.equals(certSha1Thumbprint, getSha1Thumbprint(crt));
					} catch(Exception ex) {
						throw new WrappedException(ex);
					}
				})) {
					throw new CertificateException("Trust chain can not be verified with provided server thumbprint!");
				}
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};

		return tm;
	}

	/**
	 * The default client.
	 */
	public synchronized HttpClient client() {
		HttpClient client = m_client;
		if(null == client) {
			ExecutorService ex = Executors.newCachedThreadPool();

			m_client = client = HttpClient.newBuilder()
				.executor(ex)
				.followRedirects(Redirect.NORMAL)
				.version(Version.HTTP_1_1)
				.connectTimeout(Duration.ofMinutes(1))
				.cookieHandler(new CookieManager())
				.build();
			m_clientList.add(client);
		}
		return client;
	}

	@Override
	public void close() {
		List<HttpClient> list;
		synchronized(this) {
			list = m_clientList;
			m_clientList = new ArrayList<>();
			HttpClient client = m_client;
			if(null != client && !list.contains(client)) {
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

	private void closeClient(HttpClient client) {
		System.out.println("destroy: closing http client resources");
		Optional<Executor> executorStupidity = client.executor();
		if(executorStupidity.isPresent()) {                    // Sure. This is of course better than NULL because no one would forget this. Idiots. And now the compiler cannot check.
			System.out.println("destroy: preparing to close HTTP executor");
			Executor executor = executorStupidity.get();
			if(executor instanceof ExecutorService xs) {
				xs.shutdownNow();
				System.out.println("destroy: closed HTTP executor");
			}
		}
	}

}
