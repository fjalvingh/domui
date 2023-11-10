package to.etc.net.http;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.net.SslCertificateType;
import to.etc.net.SslParameters;
import to.etc.net.SslParametersBuilder;
import to.etc.util.WrappedException;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-03-22.
 */
public class GenericHttpRequest {
	private String m_url;

	private String m_method;

	private Map<String, List<String>> m_headerMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Nullable
	private IHttpBodyProducer m_body;

	@Nullable
	private Duration m_timeout;

	@Nullable
	private HttpImpl m_clientImpl;

	@Nullable
	private SslParameters m_sslParameters;

	// CHECKSTYLE:OFF
	public GenericHttpRequest() {
	}

	public GenericHttpRequest uri(String url) {
		m_url = url;
		return this;
	}

	public GenericHttpRequest ssl(SslCertificateType type, byte[] certificate, String passkey) {
		m_sslParameters = new SslParametersBuilder().setSslType(type).setSslCertificate(certificate).setSslPasskey(passkey).build();
		return this;
	}

	public GenericHttpRequest uri(URI uri) {
		try {
			m_url = uri.toURL().toExternalForm();
			return this;
		} catch(Exception x) {
			throw WrappedException.wrap(x);            // Idiots
		}
	}

	public GenericHttpRequest header(String name, String value) {
		m_headerMap.computeIfAbsent(name, a -> new ArrayList<>()).add(value);
		return this;
	}

	public GenericHttpRequest GET() {
		m_method = "GET";
		return this;
	}

	public GenericHttpRequest body(IHttpBodyProducer body) {
		m_body = body;
		return this;
	}

	public GenericHttpRequest method(String method) {
		m_method = method;
		return this;
	}

	public GenericHttpRequest timeout(Duration timeout) {
		m_timeout = timeout;
		return this;
	}

	public GenericHttpRequest POST(IHttpBodyProducer p) {
		m_method = "POST";
		m_body = p;
		return this;
	}

	public String getUrl() {
		return m_url;
	}

	public String getMethod() {
		return m_method;
	}

	public Map<String, List<String>> getHeaderMap() {
		return m_headerMap;
	}

	public IHttpBodyProducer getBody() {
		return m_body;
	}

	public Duration getTimeout() {
		return m_timeout;
	}

	@Nullable
	public HttpImpl getClientImpl() {
		return m_clientImpl;
	}

	public GenericHttpRequest impl(HttpImpl impl) {
		m_clientImpl = impl;
		return this;
	}

	@Nullable
	public SslParameters getSslParameters() {
		return m_sslParameters;
	}
}
