package to.etc.domui.util.images.cache;

import java.io.*;
import java.util.*;

/**
 * Contains a set of references that you are working on from the
 * file cache. Entries in here are protected from being garbage-collected
 * on the file system.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 1, 2009
 */
public class FileCacheSet {
	private FileCache m_cache;

	private List<FileCacheEntry> m_contents = new ArrayList<FileCacheEntry>();

	FileCacheSet(FileCache cache) {
		m_cache = cache;
	}

	/**
	 * Get a cache entry. If one already exists this just returns the file, the use count is left
	 * at 1.
	 * @param path
	 * @return
	 */
	public File getFile(String path) {
		File	f	= new File(m_cache.getCacheRoot(), path).getAbsoluteFile();
		for(FileCacheEntry ce : m_contents) {
			if(ce.getFile().equals(f))
				return f;
		}

		//-- Allocate a new REF for here
		FileCacheEntry r = m_cache.getCacheEntry(f);
		m_contents.add(r);
		return f;
	}

	/**
	 * Discard all elements in the set (allows them to be reaped).
	 */
	public void close() {
		for(FileCacheEntry fe : m_contents) {
			m_cache.entryClosed(fe);
		}
		m_contents.clear();
	}
}
