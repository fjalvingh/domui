package to.etc.net;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-03-22.
 */
final public class SslParameters {
	final private byte[] m_sslCertificate;

	final private SslCertificateType m_sslType;

	final private String m_sslPasskey;

	public SslParameters(@NonNull SslCertificateType sslType, @NonNull byte[] sslCertificate, @Nullable String sslPasskey) {
		m_sslCertificate = sslCertificate;
		m_sslType = sslType;
		m_sslPasskey = sslPasskey;
	}

	@NonNull
	public byte[] getSslCertificate() {
		return m_sslCertificate;
	}

	@NonNull
	public SslCertificateType getSslType() {
		return m_sslType;
	}

	@Nullable
	public String getSslPasskey() {
		return m_sslPasskey;
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
		if(m_sslType != that.m_sslType)
			return false;
		return m_sslPasskey != null ? m_sslPasskey.equals(that.m_sslPasskey) : that.m_sslPasskey == null;
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(m_sslCertificate);
		result = 31 * result + m_sslType.hashCode();
		result = 31 * result + (m_sslPasskey != null ? m_sslPasskey.hashCode() : 0);
		return result;
	}
}
