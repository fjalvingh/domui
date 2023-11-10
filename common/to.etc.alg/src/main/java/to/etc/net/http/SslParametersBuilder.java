package to.etc.net.http;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.nio.charset.StandardCharsets;

@NonNullByDefault
public class SslParametersBuilder {

	@Nullable
	private SslCertificateType m_sslType;

	@Nullable
	private byte[] m_sslCertificate;

	@Nullable
	private byte[] m_certSha1Thumbprint;

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

	public SslParametersBuilder setCertSha1Thumbprint(@Nullable byte[] certSha1Thumbprint) {
		m_certSha1Thumbprint = certSha1Thumbprint;
		return this;
	}

	/**
	 * Use it to mark ssl connections to servers that we trust always.
	 */
	public SslParametersBuilder setInsecureSslThumbprint() {
		m_certSha1Thumbprint = SslParameters.INSECURE_SSL_THUMBPRINT.getBytes(StandardCharsets.UTF_8);
		return this;
	}


	public SslParametersBuilder setSslPasskey(@Nullable String sslPasskey) {
		m_sslPasskey = sslPasskey;
		return this;
	}

	public SslParameters build() {
		return new SslParameters(m_sslType, m_sslCertificate, m_sslPasskey, m_certSha1Thumbprint);
	}
}