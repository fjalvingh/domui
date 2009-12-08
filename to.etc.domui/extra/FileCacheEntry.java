package to.etc.domui.util.images.cache;

import java.io.*;

import javax.annotation.*;

public class FileCacheEntry {
	private String m_key;

	private File m_file;

	private int m_useCount;

	public FileCacheEntry(File file, String key) {
		m_file = file;
		m_key = key;
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
		m_useCount++;
	}

	boolean dec() {
		if(m_useCount == 0)
			throw new IllegalStateException("Unexpected: use count already zero");
		return --m_useCount == 0;
	}
}
