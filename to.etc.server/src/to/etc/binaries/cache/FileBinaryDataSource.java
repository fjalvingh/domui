package to.etc.binaries.cache;

import java.io.*;

import to.etc.binaries.images.*;

public class FileBinaryDataSource implements ImageDataSource {
	private File	m_file;

	private int		m_width;

	private int		m_height;

	private String	m_mimetype;

	private int		m_size;

	public FileBinaryDataSource(File file, String mimetype, int width, int height) {
		m_file = file;
		m_mimetype = mimetype;
		m_width = width;
		m_height = height;
		m_size = (int) file.length();
	}

	public File getFile() {
		return m_file;
	}

	public int getWidth() {
		return m_width;
	}

	public int getHeight() {
		return m_height;
	}

	public String getMime() {
		return m_mimetype;
	}

	public int getSize() {
		return m_size;
	}

	public int getPage() {
		return 0;
	}

	public InputStream getInputStream() throws Exception {
		return new FileInputStream(m_file);
	}

	public void discard() {
	}
}
