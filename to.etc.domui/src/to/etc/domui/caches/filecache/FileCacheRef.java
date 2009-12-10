package to.etc.domui.caches.filecache;

import java.io.*;

/**
 * A referral (use) of a single cached file. When this gets closed the corresponding
 * FileCacheEntry's use count is decremented by 1 causing it do be discardable when
 * necessary.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2009
 */
final public class FileCacheRef {
	private FileCacheEntry m_entry;

	FileCacheRef(FileCacheEntry entry) {
		m_entry = entry;
	}

	public File getFile() {
		if(m_entry == null)
			throw new IllegalStateException("The reference to this cached file has been closed");
		return m_entry.getFile();
	}

	public void close() {
		if(m_entry == null) // Allow multiple closes.
			return;
		m_entry.dec();
		m_entry = null;
	}
}
