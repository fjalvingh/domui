package to.etc.domui.util.images.cache;

import java.io.*;
import java.util.*;

/**
 * The file area cache used by the image code. This maintains the file cache and cleans it out every
 * once in a while. To ensure safe access all files in the cache <i>must</i> be retrieved through
 * this file cache, and after use these need to be released. This ensures that no files are removed
 * that are currently in use.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 30, 2009
 */
public class FileCache {
	private File m_cacheRoot;

	/** Max size in cached files (10GB default) */
	private long m_maxFileSize = 10l * 1024l * 1024l * 1024l;

	private long m_currentFileSize = 0;

	private Map<String, FileCacheEntry> m_refMap = new HashMap<String, FileCacheEntry>();

	private boolean m_reaperRunning;

	private String m_cacheRootPath;

	public FileCache(File cacheRoot, long maxFileSize) {
		m_cacheRoot = cacheRoot.getAbsoluteFile();
		m_maxFileSize = maxFileSize;
		m_cacheRootPath = m_cacheRoot.getAbsolutePath().replace('\\', '/');
		if(!m_cacheRootPath.endsWith("/"))
			m_cacheRootPath += "/";
	}

	File getCacheRoot() {
		return m_cacheRoot;
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	FileCacheEntry getCacheEntry(File f) {
		//-- Be very sure the path is OK, and make the path fully relative wrt the root of the cache (canonicalize names for repo access)
		String s = f.getAbsolutePath().replace('\\', '/');
		int pos = m_cacheRootPath.length();
		if(!m_cacheRootPath.equals(f.getAbsolutePath().substring(0, pos)))
			throw new IllegalStateException("Unexpected: input path " + f + " resulting in " + s + " is not in cache " + m_cacheRootPath);
		String key = s.substring(pos); // This is formal relative name.

		//-- Allocate a FileRef for this.
		synchronized(this) {
			FileCacheEntry fe = m_refMap.get(key);
			if(fe == null) {
				fe = new FileCacheEntry(f, key);
				m_refMap.put(key, fe);
			}
			fe.inc();
			return fe;
		}
	}

	synchronized boolean inuse(String key) {
		return m_refMap.containsKey(key);
	}

	/**
	 * Called when an entry is dereferenced. Removes the entry from the used set, allowing it to be reaped.
	 * @param ce
	 */
	synchronized void entryClosed(FileCacheEntry ce) {
		if(!ce.dec())
			return;

		//-- This entry is no longer in use.
		m_refMap.remove(ce.getKey());

		//-- Set 'last modified' for LRU cleaning pps
		try {
			if(ce.getFile().exists()) {
				ce.getFile().setLastModified(System.currentTimeMillis());
			}
		} catch(Exception x) {}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	File cache reaper.									*/
	/*--------------------------------------------------------------*/
	static private final int MAX_ENTRIES = 5000;

	/**
	 * Called every once in a while to clean out the file cache. This walks the entire cache directory and determines
	 * it's current size; it also collects the oldest files in that cache into a reap list. If the size of the
	 * cache is above it's max size then files on the reap list are deleted until the cache reaches an allowed size.
	 */
	public void reaper() {
		synchronized(this) { // Allow running only in one thread, not concurrently
			if(m_reaperRunning)
				return;
			m_reaperRunning = true;
		}

		try {
			//-- Reap until size is ok
			for(;;) {
				reapOnce();
				if(m_currentFileSize < m_maxFileSize)
					return;
			}
		} catch(Exception x) {
			System.out.println("FileCache: reaper got exception " + x);
			x.printStackTrace();
		} finally {
			synchronized(this) {
				m_reaperRunning = false;
			}
		}
	}

	static private class ReapEntry {
		public File m_file;

		public String m_key;

		public long m_modified;

		public ReapEntry(File file, String key, long modified) {
			m_file = file;
			m_key = key;
			m_modified = modified;
		}


	}

	private void reapOnce() throws Exception {
		List<ReapEntry> reaplist = new ArrayList<ReapEntry>(MAX_ENTRIES);
		m_currentFileSize = 0;
		StringBuilder sb = new StringBuilder(128);
		appendReapList(m_cacheRoot, reaplist, sb);

		int reapix = 0;
		while(m_currentFileSize >= m_maxFileSize && reapix < reaplist.size()) { // Too big and files left?
			ReapEntry re = reaplist.get(reapix++);
			synchronized(this) {
				if(!inuse(re.m_key)) {
					m_currentFileSize -= re.m_file.length();
					re.m_file.delete();
				}
			}
		}
	}

	private void appendReapList(File dir, List<ReapEntry> reaplist, StringBuilder sb) {
		int len = sb.length();
		File[] far = dir.listFiles();
		for(File f : far) {
			sb.setLength(len);
			if(len > 0)
				sb.append('/');
			sb.append(f.getName());

			if(f.isDirectory()) {
				appendReapList(f, reaplist, sb);
			} else {
				m_currentFileSize += f.length();

				String key = sb.toString();
				if(!inuse(key)) { // race condition here is ok, will be fixed when actually deleting stuff
					long lm = f.lastModified();

					if(reaplist.size() >= MAX_ENTRIES)
						reaplist.remove(reaplist.size() - 1);
					insertReapEntry(reaplist, new ReapEntry(f, sb.toString(), lm));
				}
			}
		}
	}

	final static private Comparator<ReapEntry> C_COMP = new Comparator<ReapEntry>() {
		@Override
		public int compare(ReapEntry o1, ReapEntry o2) {
			long r = o2.m_modified - o1.m_modified;
			if(r == 0)
				return 0;
			return r < 0 ? -1 : 1;
		}
	};

	private void insertReapEntry(List<ReapEntry> reaplist, ReapEntry reapentry) {
		int ix = Collections.binarySearch(reaplist, reapentry, C_COMP);
		if(ix < 0) {
			ix = -(ix + 1);
		}
		reaplist.add(ix, reapentry);
	}
}
