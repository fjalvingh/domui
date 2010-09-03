package to.etc.domui.util;

import java.io.*;
import java.sql.*;

/**
 * An abstracted version so that we can use it to set a new value into
 * a BLOB column.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 9, 2008
 */
abstract public class AbstractBlob implements Blob {
	@Override
	abstract public InputStream getBinaryStream() throws SQLException;

	@Override
	public byte[] getBytes(long pos, int length) throws SQLException {
		throw new IllegalStateException("Do not call this - it is very inefficient");
	}

	@Override
	public long length() throws SQLException {
		return 0;
	}

	@Override
	public long position(byte[] pattern, long start) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public long position(Blob pattern, long start) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public OutputStream setBinaryStream(long pos) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public void truncate(long len) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public void free() throws SQLException {}

	@Override
	public InputStream getBinaryStream(long pos, long length) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}
}
