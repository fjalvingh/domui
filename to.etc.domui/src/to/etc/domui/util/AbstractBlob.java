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
 * An abstracted version so that we can use it to set a new value into
 * a BLOB column.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 9, 2008
 */
abstract public class AbstractBlob implements Blob {
	@Override
	abstract public InputStream getBinaryStream() throws SQLException;

	@Override
	public byte[] getBytes(long pos, int length) throws SQLException {
		throw new IllegalStateException("Do not call this - it is very inefficient");
	}

	@Override
	public long length() throws SQLException {
		return 0;
	}

	@Override
	public long position(byte[] pattern, long start) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public long position(Blob pattern, long start) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public OutputStream setBinaryStream(long pos) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public void truncate(long len) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}

	@Override
	public void free() throws SQLException {}

	@Override
	public InputStream getBinaryStream(long pos, long length) throws SQLException {
		throw new IllegalStateException("Do not call this");
	}
}
