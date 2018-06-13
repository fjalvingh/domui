/*
 * DomUI Java User Interface - shared code
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
package to.etc.util;

import java.io.*;

/**
 * <p>A buffer which accepts data and buffers it. The data can be obtained
 * again by reading an input stream. If the data grows too large (user
 * definable) it can flush the data into a tempfile. This class is <b>not</b>
 * threadsafe of course.</p>
 * <p>Because this class may retain a file it is important that it gets
 * cleaned up after use by calling discard().</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 10, 2006
 */
public class FileBackedBuffer extends OutputStream {
	static private final byte[]	NODATA	= new byte[0];

	/** The max. #bytes that this buffer may retain in memory before it flushes the data to disk. */
	private int					m_maxInMemory;

	private int					m_initialAllocation;

	private boolean				m_inFile;

	/** If a file was allocated this holds on to it. */
	private File				m_file;

	private byte[]				m_data	= NODATA;

	/** The current size of the buffer, in bytes. */
	private int					m_size;

	/** T if the writer has closed. */
	private boolean				m_wclosed;

	/** If the file is being used as a buffer this is the file's output. */
	private OutputStream		m_fos;

	/** If someone allocated an input stream from this this holds it. */
	InputStream					m_is;

	public FileBackedBuffer() {
		this(8192);
	}

	public FileBackedBuffer(int size) {
		this(size, size <= 8192 ? size : 8192);
	}

	public FileBackedBuffer(int size, int initial) {
		assert (size >= 0 && initial > 0);
		m_maxInMemory = size;
		m_initialAllocation = initial;
	}

	/**
	 * This MUST be called after use. It releases all resources
	 * held by this object.
	 */
	public void discard() {
		if(m_fos != null) {
			try {
				m_fos.close();
			} catch(Exception x) {}
			m_fos = null;
		}
		if(m_is != null) {
			try {
				m_is.close();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
		if(m_file != null) {
			try {
				m_file.delete();
			} catch(Exception x) {}
			m_file = null;
		}
	}

	/**
	 * This finalizer at least tries to cleanup the mess if discard() was not called...
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		discard();
		super.finalize();
	}

	/*--------------------------------------------------------------*/
	/* CODING: Writer code.                                         */
	/*--------------------------------------------------------------*/
	/**
	 * This closes the write channel. After this the data can be read
	 * using getInputStream() or another call.
	 *
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		if(m_wclosed)
			return;
		super.close();
		if(m_fos != null) {
			m_fos.close();
			m_fos = null;
		}
		m_wclosed = true;
	}

	/**
	 * Called when the internal buffer has overflown. It allocates a new buffer.
	 * @param newsz
	 */
	private void reallocate(int newsz) {
		if(newsz < m_initialAllocation)
			newsz = m_initialAllocation;
		else {
			newsz *= 2;
			if(newsz > m_maxInMemory / 2)
				newsz = m_maxInMemory;
		}
		byte[] old = m_data;
		m_data = new byte[newsz];
		System.arraycopy(old, 0, m_data, 0, m_size);
	}

	private void startFileWriter() throws IOException {
		if(m_file == null) { // Need to allocate a tempfile?
			m_file = File.createTempFile("filebuffer", "tmp");
		}
		m_inFile = true;
		m_fos = new FileOutputStream(m_file);
		m_fos.write(m_data, 0, m_size);
		m_data = NODATA;
	}

	public File asFile() throws IOException {
		if(m_file == null) {
			startFileWriter();
		}
		return m_file;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if(m_fos != null) { // The file buffer is used.
			m_fos.write(b, off, len);
			m_size += len;
			return;
		}
		if(m_wclosed)
			throw new IOException("The buffer has been closed.");
		int newsize = m_size + len;
		if(newsize >= m_maxInMemory) { // This would reach the max. available?
			startFileWriter();
			m_fos.write(b, off, len);
			m_size += len;
			return;
		}
		if(newsize >= m_data.length) { // Need to reallocate?
			reallocate(newsize);
		}
		System.arraycopy(b, off, m_data, m_size, len);
		m_size += len;
	}

	@Override
	public void write(int b) throws IOException {
		if(m_fos != null) { // The file buffer is used.
			m_fos.write(b);
			m_size++;
			return;
		}
		if(m_wclosed)
			throw new IOException("The buffer has been closed.");
		int newsize = m_size + 1;
		if(newsize >= m_maxInMemory) { // This would reach the max. available?
			startFileWriter();
			m_fos.write(b);
			m_size++;
			return;
		}
		if(newsize >= m_data.length) { // Need to reallocate?
			reallocate(newsize);
		}
		m_data[m_size] = (byte) b;
		m_size++;
	}

	/**
	 * Removes all contents from this thingy, and reopens it as an
	 * output stream.
	 */
	public void clear() {
		if(m_fos != null) {
			try {
				m_fos.close();
			} catch(Exception x) {
				x.printStackTrace();
			}
			m_fos = null;
		}
		if(m_is != null) {
			try {
				m_is.close();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
		m_inFile = false;
		m_wclosed = false;
		m_size = 0;
	}

	/*--------------------------------------------------------------*/
	/* CODING: Reader code.                                         */
	/*--------------------------------------------------------------*/
	private void checkReadable() throws IOException {
		if(!m_wclosed)
			throw new IllegalStateException("The writer has not yet been closed.");
		if(m_is != null)
			throw new IllegalStateException("The previous input stream has not yet been closed");
	}

	/**
	 * Returns an inputstream which reads this. This always returns the
	 * same stream instance(!) so it cannot be called >1ce while the
	 * previous stream is still open. If you do you'll get exceptioned.
	 */
	public InputStream getInputStream() throws IOException {
		checkReadable();

		//-- If the data resided in a file return a filereader and capture the close.
		if(m_inFile) {
			m_is = new FileInputStream(m_file) {
				/**
				 * This embedded close clears the input stream indicator.
				 * @see java.io.FileInputStream#close()
				 */
				@Override
				public void close() throws IOException {
					m_is = null;
					super.close();
				}
			};
		} else {
			m_is = new ByteArrayInputStream(m_data, 0, m_size) {
				/**
				 * This embedded close clears the input stream indicator.
				 * @see java.io.FileInputStream#close()
				 */
				@Override
				public void close() throws IOException {
					m_is = null;
					super.close();
				}
			};
		}
		return m_is;
	}

	public int size() {
		return m_size;
	}

	/**
	 * Sends all of the contained data to the outputstream.
	 * @param os
	 */
	public void copy(OutputStream os) throws IOException {
		checkReadable();
		if(!m_inFile) {
			os.write(m_data, 0, m_size);
		} else {
			InputStream is = getInputStream();
			try {
				FileTool.copyFile(os, is);
			} finally {
				is.close();
			}
		}
	}

}
