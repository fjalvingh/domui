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
