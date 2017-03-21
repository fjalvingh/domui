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
package to.etc.domui.caches;

import java.io.*;

import javax.annotation.*;

import to.etc.domui.util.images.*;

public class FileImageReference implements IImageReference {
	private File m_source;

	private String m_mime;

	public FileImageReference(File source, String mime) {
		m_source = source;
		m_mime = mime;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public InputStream getInputStream() throws Exception {
		return new FileInputStream(m_source);
	}

	@Override
	public @Nonnull String getMimeType() throws Exception {
		return m_mime;
	}

	@Override
	public long getVersionLong() throws Exception {
		return 0;
	}
}
