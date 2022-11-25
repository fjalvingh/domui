package to.etc.net;

import java.util.List;

import static java.util.List.of;

public enum SslCertificateType {
	PFX("SunX509", "PKCS12", "TLSv1.2", of(".pfx"));

	private final String m_keyManagerAlgorithm;

	private final String m_keyStoreType;

	private final String m_sslContextProtocol;

	private final List<String> m_extensions;

	SslCertificateType(String keyManagerAlgorithm, String keyStoreType, String sslContextProtocol, List<String> extensions) {
		m_keyManagerAlgorithm = keyManagerAlgorithm;
		m_keyStoreType = keyStoreType;
		m_sslContextProtocol = sslContextProtocol;
		m_extensions = extensions;
	}

	public String getKeyManagerAlgorithm() {
		return m_keyManagerAlgorithm;
	}

	public String getKeyStoreType() {
		return m_keyStoreType;
	}

	public List<String> getExtensions() {
		return m_extensions;
	}

	public String getSslContextProtocol() {
		return m_sslContextProtocol;
	}
}
