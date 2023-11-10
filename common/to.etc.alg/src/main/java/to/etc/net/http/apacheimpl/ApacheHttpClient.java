package to.etc.net.http.apacheimpl;

import to.etc.net.http.BodyProducers.EmptyBodyProducer;
import to.etc.net.http.BodyProducers.StringBodyProducer;
import to.etc.net.http.GenericHttpHeaders;
import to.etc.net.http.GenericHttpRequest;
import to.etc.net.http.GenericHttpResponse;
import to.etc.net.http.IBodyReader;
import to.etc.net.http.IHttpBodyProducer;
import to.etc.net.http.IHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.RequestConfig.Builder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.net.SslCertificateType;
import to.etc.net.SslParameters;
import to.etc.util.FileTool;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-03-22.
 */
public class ApacheHttpClient implements IHttpClient {
	private List<CloseableHttpClient> m_clientList = new ArrayList<>();

	private Map<SslParameters, CloseableHttpClient> m_sslClientMap = new HashMap<>();

	@Override
	public <T> GenericHttpResponse<T> send(GenericHttpRequest request, IBodyReader<T> reader) throws Exception {
		String method = request.getMethod();
		if(null == method) {
			method = "GET";
		}
		switch(method.toUpperCase()) {
			default:
				throw new IllegalStateException("Method '" + request.getMethod() + "' not implemented");

			case "GET":
				return handleGet(request, reader);

			case "POST":
				return handlePost(request, reader);
		}
	}

	private <T> GenericHttpResponse<T> handlePost(GenericHttpRequest request, IBodyReader<T> reader) throws Exception {
		AbstractHttpEntity postEntity = createPostEntity(request.getBody());
		HttpPost post = new HttpPost(request.getUrl());
		post.setEntity(postEntity);
		request.getHeaderMap().forEach((name, list) -> {
			for(String s : list) {
				post.addHeader(name, s);
			}
		});

		Builder cb = RequestConfig.custom();
		Duration timeout = request.getTimeout();
		if(timeout != null) {
			cb.setResponseTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
			cb.setConnectionRequestTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
			cb.setConnectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
		}
		post.setConfig(cb.build());
		CloseableHttpClient client = client(request);
		try {
			CloseableHttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			GenericHttpHeaders gh = new GenericHttpHeaders();
			for(Header header : response.getHeaders()) {
				gh.put(header.getName(), header.getValue());
			}

			GenericHttpResponse<T> result = new GenericHttpResponse<>(response.getCode(), gh, getContent(entity, reader), client, response);
			client = null;								// Ownership of client passes to caller; it must close the response
			return result;
		} finally {
			FileTool.closeAll(client);					// Close client if not used
		}
	}

	private <T> T getContent(@Nullable HttpEntity entity, IBodyReader<T> reader) throws Exception {
		if(reader.getTypeClass() == String.class) {
			return (T) (entity == null ? "" : EntityUtils.toString(entity));
		} else if(reader.getTypeClass() == InputStream.class) {
			return (T) (entity == null ? InputStream.nullInputStream() : entity.getContent());
		} else {
			throw new IllegalStateException("Unsupported body reader: " + reader.getTypeClass().getName());
		}
	}

	private CloseableHttpClient client(GenericHttpRequest r) throws Exception {
		SslParameters ssl = r.getSslParameters();
		if(null == ssl) {
			return defaultClient();
		} else {
			return sslClient(ssl, r);
		}
	}

	/**
	 * Allocate a new SSL client. We do not cache these for Apache as they
	 * are not usable by multiple clients.
	 */
	private synchronized CloseableHttpClient sslClient(SslParameters ssl, GenericHttpRequest r) throws Exception {
		CloseableHttpClient cl = createSslClient(ssl, r);
		m_clientList.add(cl);
		return cl;
	}

	private CloseableHttpClient createSslClient(SslParameters ssl, GenericHttpRequest r) throws Exception {
		SslCertificateType sslType = requireNonNull(ssl.getSslType(), "sslType must be set on ssl");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(sslType.getKeyManagerAlgorithm());
		KeyStore keystore = KeyStore.getInstance(sslType.getKeyStoreType());

		byte[] sslCertificate = requireNonNull(ssl.getSslCertificate(), "sslCertificate must be set on ssl");
		try(InputStream is = new ByteArrayInputStream(sslCertificate)) {
			String passkey = ssl.getSslPasskey();
			char[] passkeyArray = null != passkey ? passkey.toCharArray() : null;
			keystore.load(is, passkeyArray);
			kmf.init(keystore, passkeyArray);

			SSLContext sslContext = SSLContext.getInstance(sslType.getSslContextProtocol());
			sslContext.init(kmf.getKeyManagers(), null, null);

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
			Registry<ConnectionSocketFactory> socketFactoryRegistry =
				RegistryBuilder.<ConnectionSocketFactory>create()
					.register("https", sslsf)
					.register("http", new PlainConnectionSocketFactory())
					.build();
			BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);

			Duration timeout = r.getTimeout();
			RequestConfig.Builder cb = RequestConfig.custom();
			if(timeout != null) {
				cb.setResponseTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
				cb.setConnectionRequestTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
				cb.setConnectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);

				SocketConfig socketConfigShit = SocketConfig.custom()
					.setSoTimeout((int) timeout.toMillis(), TimeUnit.MILLISECONDS)
					.build();
				connectionManager.setSocketConfig(socketConfigShit);
				System.out.println("Updating socketTimeout to " + timeout);
			}

			CloseableHttpClient cl = HttpClientBuilder.create()
				.setConnectionManager(connectionManager)
				.setDefaultRequestConfig(cb.build())
				.build();

			return cl;
		}
	}

	private synchronized CloseableHttpClient defaultClient() {
		if(m_clientList.isEmpty()) {
			CloseableHttpClient hc = HttpClients.createDefault();
			m_clientList.add(hc);
		}
		return m_clientList.get(0);
	}

	private AbstractHttpEntity createPostEntity(IHttpBodyProducer bodyProducer) {
		if(bodyProducer == null || bodyProducer instanceof EmptyBodyProducer) {
			return new StringEntity("");
		} else if(bodyProducer instanceof StringBodyProducer) {
			return new StringEntity(((StringBodyProducer) bodyProducer).getData());
		} else {
			throw new IllegalStateException("Unimplemented body producer: " + bodyProducer.getClass().getName());
		}
	}

	private <T> GenericHttpResponse<T> handleGet(GenericHttpRequest request, IBodyReader<T> reader) throws Exception {
		HttpGet get = new HttpGet(request.getUrl());
		request.getHeaderMap().forEach((name, list) -> {
			for(String s : list) {
				get.addHeader(name, s);
			}
		});

		Builder cb = RequestConfig.custom();
		Duration timeout = request.getTimeout();
		if(timeout != null) {
			cb.setResponseTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
			cb.setConnectionRequestTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
			cb.setConnectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
		}
		get.setConfig(cb.build());
		CloseableHttpResponse response = client(request).execute(get);
		HttpEntity entity = response.getEntity();
		GenericHttpHeaders gh = new GenericHttpHeaders();
		for(Header header : response.getHeaders()) {
			gh.put(header.getName(), header.getValue());
		}

		return new GenericHttpResponse<>(response.getCode(), gh, getContent(entity, reader));
	}

	@Override
	public void close() {
		List<CloseableHttpClient> list;
		synchronized(this) {
			list = m_clientList;
			m_clientList = new ArrayList<>();
		}
		for(CloseableHttpClient httpClient : list) {
			try {
				httpClient.close();
			} catch(Exception x) {
				System.err.println("ApacheHTTP: failed to close client: " + x);
			}
		}
	}
}
