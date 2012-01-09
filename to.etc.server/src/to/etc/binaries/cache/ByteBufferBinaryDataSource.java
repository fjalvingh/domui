package to.etc.binaries.cache;

import java.io.*;

import to.etc.binaries.images.*;
import to.etc.util.*;

public class ByteBufferBinaryDataSource implements ImageDataSource {
	private byte[][]	m_buffers;

	private File		m_file;

	private int			m_width;

	private int			m_height;

	private String		m_mimetype;

	private int			m_size;

	public ByteBufferBinaryDataSource(byte[][] buffers, String mimetype, int width, int height, int size) {
		m_buffers = buffers;
		m_mimetype = mimetype;
		m_width = width;
		m_height = height;
		m_size = size;
	}

	public ByteBufferBinaryDataSource(byte[][] buffers, String mimetype, int width, int height) {
		m_buffers = buffers;
		m_mimetype = mimetype;
		m_width = width;
		m_height = height;
		m_size = 0;
		for(byte[] b : m_buffers)
			m_size += b.length;
	}

	public File getFile() throws IOException {
		if(m_file == null) {
			File of = BinariesCache.makeTempFile("tmp");
			FileTool.save(of, m_buffers);
			m_file = of;
		}
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

	public InputStream getInputStream() {
		return new ByteBufferInputStream(m_buffers);
	}

	public void discard() {
	}
}
