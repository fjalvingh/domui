package to.etc.net.http;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class SslParametersBuilder {

	@Nullable
	private SslCertificateType m_sslType;

	@Nullable
	private byte[] m_sslCertificate;

	@Nullable
	private byte[] m_serverThumbprint;

	@Nullable
	private String m_sslPasskey;

	public SslParametersBuilder setSslType(@Nullable SslCertificateType sslType) {
		m_sslType = sslType;
		return this;
	}

	public SslParametersBuilder setSslCertificate(@Nullable byte[] sslCertificate) {
		m_sslCertificate = sslCertificate;
		return this;
	}

	public SslParametersBuilder setServerThumbprint(@Nullable byte[] serverThumbprint) {
		m_serverThumbprint = serverThumbprint;
		return this;
	}

	public SslParametersBuilder setSslPasskey(@Nullable String sslPasskey) {
		m_sslPasskey = sslPasskey;
		return this;
	}

	public SslParameters build() {
		return new SslParameters(m_sslType, m_sslCertificate, m_sslPasskey, m_serverThumbprint);
	}
}