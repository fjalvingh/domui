package to.etc.domui.util;

import to.etc.util.ByteBufferInputStream;

import java.io.InputStream;
import java.sql.SQLException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 5-12-18.
 */
public class ByteBufferBlob extends AbstractBlob {
	private final byte[][] m_buffer;

	public ByteBufferBlob(byte[][] buffer) {
		m_buffer = buffer;
	}

	@Override
	public InputStream getBinaryStream() throws SQLException {
		return new ByteBufferInputStream(m_buffer);
	}

	@Override
	public long length() throws SQLException {
		long len = 0;
		for(byte[] bytes : m_buffer) {
			len += bytes.length;
		}
		return len;
	}

}
