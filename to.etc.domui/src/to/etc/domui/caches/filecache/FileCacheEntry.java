package to.etc.domui.caches.filecache;

import java.io.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

class FileCacheEntry {
	private FileCache m_cache;

	private String m_key;

	private File m_file;

	@GuardedBy("m_cache")
	int m_useCount;

	public FileCacheEntry(File file, String key) {
		m_file = file;
		m_key = key;
		m_useCount = 1;
	}

	@Nonnull
	public String getKey() {
		return m_key;
	}

	@Nonnull
	public File getFile() {
		return m_file;
	}

	void inc() {
		m_cache.incUse(this);
	}

	void dec() {
		m_cache.decUse(this);
	}
}
