package to.etc.net.http;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-03-22.
 */
@NonNullByDefault
final public class SslParameters {

	public static final String INSECURE_SSL_THUMBPRINT = "insecure ssl context thumbprint";

	@Nullable
	private final byte[] m_sslCertificate;

	/**
	 * Pins the specified HTTPS URL Connection to work against a specific server-side certificate with the specified thumbprint only.
	 */
	@Nullable
	private final byte[] m_certSha1Thumbprint;

	@Nullable
	private final SslCertificateType m_sslType;

	@Nullable
	private final String m_sslPasskey;

	SslParameters(@Nullable SslCertificateType sslType, @Nullable byte[] sslCertificate, @Nullable String sslPasskey, @Nullable byte[] certSha1Thumbprint) {
		m_sslCertificate = sslCertificate;
		m_sslType = sslType;
		m_sslPasskey = sslPasskey;
		m_certSha1Thumbprint = certSha1Thumbprint;
	}

	@Nullable
	public byte[] getSslCertificate() {
		return m_sslCertificate;
	}

	@Nullable
	public SslCertificateType getSslType() {
		return m_sslType;
	}

	@Nullable
	public String getSslPasskey() {
		return m_sslPasskey;
	}

	@Nullable
	public byte[] getCertSha1Thumbprint() {
		return m_certSha1Thumbprint;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;

		SslParameters that = (SslParameters) o;

		if(!Arrays.equals(m_sslCertificate, that.m_sslCertificate))
			return false;
		if(!Arrays.equals(m_certSha1Thumbprint, that.m_certSha1Thumbprint))
			return false;
		if(m_sslType != that.m_sslType)
			return false;
		return m_sslPasskey != null ? m_sslPasskey.equals(that.m_sslPasskey) : that.m_sslPasskey == null;
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(m_sslCertificate);
		result = 31 * result + Arrays.hashCode(m_certSha1Thumbprint);
		result = 31 * result + (m_sslType != null ? m_sslType.hashCode() : 0);
		result = 31 * result + (m_sslPasskey != null ? m_sslPasskey.hashCode() : 0);
		return result;
	}
}
