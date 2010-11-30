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
package to.etc.domui.server.reloader;

import java.io.*;
import java.util.*;

import to.etc.domui.util.resources.*;
import to.etc.util.*;

/**
 * A reference to a .jar file containing some resource. This has special code to handle
 * resources loaded from a jar to prevent per-classloader caching of loaded resources.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
public class ClasspathJarRef implements IModifyableResource {
	private File m_src;

	private long m_resourceLoaderTS;

	private Map<String, byte[][]> m_cachedMap = new HashMap<String, byte[][]>();

	public ClasspathJarRef(File src) {
		m_src = src;
	}

	@Override
	public long getLastModified() {
		try {
			if(!m_src.exists())
				return -1;
			return m_src.lastModified();
		} catch(Exception x) {
			return -1;
		}
	}

	/**
	 * In debug mode, this tries to read the specified resource from the .jar file and
	 * caches it. This does an explicit test for the jar being changed and clears the
	 * cache if it has.
	 *
	 * @param relname
	 * @return
	 */
	private synchronized byte[][] getCachedResource(String relname) throws IOException {
		//-- 1. Has the jar changed since last time?
		long cts = m_src.lastModified();
		if(m_resourceLoaderTS != cts) {
			//-- Jar changed - reset
			m_resourceLoaderTS = cts;
			m_cachedMap.clear();
		}

		//-- Load the entry
		byte[][] bufs = m_cachedMap.get(relname);
		if(bufs == null) {
			bufs = loadFromJar(relname);
			if(bufs == null)
				throw new IOException("Jar file entry " + relname + " not found in jar " + m_src);
			m_cachedMap.put(relname, bufs);
		}
		return bufs;
	}

	/**
	 * Load the specified resource from the .jar file, as a set of byte buffers.
	 * @param name
	 * @return
	 * @throws IOException
	 */
	private byte[][] loadFromJar(String name) throws IOException {
		InputStream is = FileTool.getZipContent(m_src, name);
		try {
			return FileTool.loadByteBuffers(is); // Load as a set of byte buffers.
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	public InputStream getResource(String relname) throws IOException {
		return new ByteBufferInputStream(getCachedResource(relname));
	}

	@Override
	public String toString() {
		return "[ClasspathJar: " + m_src.toString() + "]";
	}
}
