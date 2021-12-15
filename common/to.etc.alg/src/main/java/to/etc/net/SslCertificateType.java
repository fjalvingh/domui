package to.etc.net;

public enum SslCertificateType {
	PFX("SunX509", "PKCS12", "pfx")
	;

	private final String m_keyManagerAlgorithm;
	private final String m_keyStoreType;
	private final String m_extensions;

	SslCertificateType(String keyManagerAlgorithm, String keyStoreType, String extensions) {
		m_keyManagerAlgorithm = keyManagerAlgorithm;
		m_keyStoreType = keyStoreType;
		m_extensions = extensions;
	}

	public String getKeyManagerAlgorithm() {
		return m_keyManagerAlgorithm;
	}

	public String getKeyStoreType() {
		return m_keyStoreType;
	}

	public String getExtensions() {
		return m_extensions;
	}
}
