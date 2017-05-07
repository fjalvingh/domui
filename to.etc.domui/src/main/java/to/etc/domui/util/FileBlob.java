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
package to.etc.domui.util;

import java.io.*;
import java.sql.*;

/**
 * A BLOB class which can be used to set the value of a BLOB property from
 * a source file. BLOB thingies are unmutable once set.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 9, 2008
 */
public class FileBlob extends AbstractBlob {
	private File m_src;

	//	private boolean			m_deleteOnStore;

	public FileBlob(File src) {
		m_src = src;
	}

	@Override
	public InputStream getBinaryStream() throws SQLException {
		try {
			return new FileInputStream(m_src);
		} catch(FileNotFoundException x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public long length() throws SQLException {
		return m_src.length();
	}
}
