package to.etc.server.vfs;

import java.io.*;

/**
 * Base class for sources that reside within a database. The lookup of a source
 * will issue a database statement; opening the stream will issue another one which
 * opens the BLOB for reading.
 *
 * @author jal
 * Created on Dec 5, 2005
 */
abstract public class VfsDatabaseProviderBase implements VfsProvider {
	private int	m_expiry;

	private int	m_checkInterval;

	abstract public InputStream getInputStream(VfsSource vs) throws Exception;

	abstract public boolean accept(VfsKey key);

	abstract public VfsSource makeSource(VfsKey ref) throws Exception;

	public VfsDatabaseProviderBase(int expiry, int chkint) {
		m_expiry = expiry;
		m_checkInterval = chkint;
	}

	public int getCheckInterval() {
		return m_checkInterval;
	}

	public int getExpiry(VfsSource vs) {
		return m_expiry;
	}

	/**
	 * The source path is the same as the vfs path for database records.
	 *
	 * @see to.etc.server.vfs.VfsProvider#getRealPath(to.etc.server.vfs.VfsReference)
	 */
	public String getRealPath(VfsKey sr) {
		return getVfsPath(sr);
	}

	public String getVfsPath(VfsKey sr) {
		return VFS.getInstance().resolveKey(sr);
	}

	public boolean needCache(VfsSource vs) {
		return true;
	}
}
