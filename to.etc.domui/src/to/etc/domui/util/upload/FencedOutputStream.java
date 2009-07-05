package to.etc.domui.util.upload;

import java.io.*;

import to.etc.util.*;

public class FencedOutputStream extends OutputStream {
	private ByteArrayOutputStream m_bbos;

	private File m_repos;

	private File m_file;

	private FileOutputStream m_fos;

	private int m_fenceSize;

	private int m_size;

	private byte[] m_buffer;

	public FencedOutputStream(File repos, int fence) {
		m_repos = repos;
		m_fenceSize = fence;
		m_bbos = new ByteArrayOutputStream();
	}

	@Override
	public void close() throws IOException {
		try {
			if(m_fos != null)
				m_fos.close();
		} catch(Exception x) {}
		try {
			if(m_bbos != null)
				m_bbos.close();
		} catch(Exception x) {}
		if(m_bbos != null) {
			m_buffer = m_bbos.toByteArray();
			m_bbos = null;
		}
	}

	@Override
	public void write(int b) throws IOException {
		OutputStream os = checkOutput(1);
		os.write(b);
		m_size++;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		OutputStream os = checkOutput(len);
		os.write(b, off, len);
		m_size += len;
	}

	@Override
	public void write(byte[] b) throws IOException {
		OutputStream os = checkOutput(b.length);
		os.write(b);
		m_size += b.length;
	}

	private OutputStream checkOutput(int len) throws IOException {
		if(m_fos != null)
			return m_fos;
		if(m_bbos.size() + len <= m_fenceSize)
			return m_bbos;

		//-- Output buffer exhausted! Swap to file!
		m_file = FileTool.makeTempFile(m_repos);
		m_fos = new FileOutputStream(m_file); // The output thingy.
		m_fos.write(m_bbos.toByteArray());
		m_bbos = null; // Discard old buffer.
		return m_fos;
	}

	/**
	 * Return T if the whole thing is in memory.
	 * @return
	 */
	final public boolean isMemory() {
		return m_bbos != null || m_buffer != null;
	}

	final public byte[] getBuffer() {
		if(m_buffer == null)
			throw new IllegalStateException("The content of this item is no longer in memory");
		return m_buffer;
	}

	/**
	 * Returns the size, in bytes, of the written thing.
	 * @return
	 */
	final public int size() {
		return m_size;
	}

	final public File getFile() {
		return m_file;
	}
}
