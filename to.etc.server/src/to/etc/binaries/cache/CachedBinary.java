package to.etc.binaries.cache;

import java.io.*;

final class CachedBinary {
	private File		m_file;

	private byte[][]	m_data;

	private BinaryInfo	m_bi;

	public CachedBinary(BinaryInfo bi, File file) {
		super();
		m_bi = bi;
		m_file = file;
	}

	public CachedBinary(BinaryInfo bi, byte[][] data) {
		super();
		m_bi = bi;
		m_data = data;
	}

	public File getFile() {
		return m_file;
	}

	public byte[][] getData() {
		return m_data;
	}

	public BinaryInfo getBi() {
		return m_bi;
	}
}
