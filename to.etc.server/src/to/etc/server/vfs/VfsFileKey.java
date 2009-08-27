package to.etc.server.vfs;

import java.io.*;

/**
 * Generic full-class file key.
 * 
 * @author jal
 * Created on Jan 20, 2006
 */
final public class VfsFileKey implements VfsKey {
	private File	m_file;

	private String	m_encoding;

	public VfsFileKey(File key, String encoding) {
		if(key == null)
			throw new IllegalStateException("The input key cannot be null");
		m_encoding = encoding == null ? "utf-8" : encoding;
		m_file = key;
	}

	public File getFile() {
		return m_file;
	}

	@Override
	public String toString() {
		return VFS.getInstance().resolveKey(this);
	}

	@Override
	public int hashCode() {
		return m_file.hashCode() + m_encoding.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof VfsFileKey) && ((VfsFileKey) obj).m_file.equals(m_file) && ((VfsFileKey) obj).m_encoding.equals(m_encoding);
	}

	public String getEncoding() {
		return m_encoding;
	}
}
