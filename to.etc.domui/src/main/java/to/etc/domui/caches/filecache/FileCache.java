/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.caches.filecache;

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
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "FindBugs definition is wrong for mkdirs, and delete() may fail in code here")
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

	public void initialize() throws Exception {
		m_cacheRoot.mkdirs();
		if(!m_cacheRoot.exists() || !m_cacheRoot.isDirectory())
			throw new IOException(m_cacheRoot + ": file cache root cannot be created or is not a directory.");
	}

	/**
	 * Internal: allocate a cache entry and return it, with an incremented use count.
	 * @param path
	 * @return
	 */
	FileCacheEntry getCacheEntry(String rpath) {
		//-- Check path for validity: cannot start with /, cannot contain '..'.
		if(rpath.contains(".."))
			throw new IllegalStateException("Invalid path: cannot contain ..");
		if(rpath.contains(":") || rpath.startsWith("/"))
			throw new IllegalStateException("Invalid path: cannot be absolute");

		File f = new File(getCacheRoot(), rpath);
		f.getParentFile().mkdirs();

		//-- Allocate a FileRef for this.
		synchronized(this) {
			FileCacheEntry fe = m_refMap.get(rpath);
			if(fe == null) {
				fe = new FileCacheEntry(this, f, rpath);
				m_refMap.put(rpath, fe);
			} else
				fe.inc();
			return fe;
		}
	}

	public FileCacheRef getFile(String rpath) {
		FileCacheEntry ce = getCacheEntry(rpath);
		return new FileCacheRef(ce);
	}

	synchronized boolean inuse(String key) {
		return m_refMap.containsKey(key);
	}

	/**
	 * Increment use count.
	 * @param ce
	 */
	synchronized void incUse(FileCacheEntry ce) {
		if(ce.m_useCount <= 0)
			throw new IllegalStateException("Internal: use count invalid");
		ce.m_useCount++;
	}

	synchronized void decUse(FileCacheEntry ce) {
		if(ce.m_useCount <= 0)
			throw new IllegalStateException("Internal: use count invalid");
		if(--ce.m_useCount > 0)
			return;

		//-- Discard this entry: usecount has reached 0.
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


	public synchronized long getCurrentFileSize() {
		return m_currentFileSize;
	}
}
