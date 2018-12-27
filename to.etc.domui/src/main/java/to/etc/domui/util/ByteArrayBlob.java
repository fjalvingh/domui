package to.etc.domui.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 5-12-18.
 */
public class ByteArrayBlob extends AbstractBlob {
	private byte[] m_data;

	public ByteArrayBlob(byte[] data) {
		m_data = data;
	}

	@Override
	public InputStream getBinaryStream() throws SQLException {
		return new ByteArrayInputStream(m_data);
	}

	@Override
	public long length() throws SQLException {
		return m_data.length;
	}
}
