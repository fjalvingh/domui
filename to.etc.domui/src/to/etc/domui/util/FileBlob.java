package to.etc.domui.util;

import java.io.*;
import java.sql.*;

/**
 * A BLOB class which can be used to set the value of a BLOB property from
 * a source file. BLOB thingies are unmutable once set.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 9, 2008
 */
public class FileBlob extends AbstractBlob {
	private File m_src;

	//	private boolean			m_deleteOnStore;

	public FileBlob(File src) {
		m_src = src;
	}

	@Override
	public InputStream getBinaryStream() throws SQLException {
		try {
			return new FileInputStream(m_src);
		} catch(FileNotFoundException x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public long length() throws SQLException {
		return m_src.length();
	}
}
