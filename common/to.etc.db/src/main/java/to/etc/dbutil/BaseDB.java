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
package to.etc.dbutil;

import java.io.*;
import java.sql.*;


abstract public class BaseDB {
	final private String m_name;

	protected BaseDB(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	/**
	 * Returns a sequence number that can be used to create a new PK for a
	 * record in the given table. Sequences are emulated on databases that do
	 * not support 'm.
	 */
	abstract protected int getSequenceID(Connection dbc, String tablename) throws SQLException;

	/**
	 * Returns a sequence number that can be used to create a new PK for a
	 * record in the given table. Sequences are emulated on databases that do
	 * not support 'm.
	 */
	abstract protected int getFullSequenceID(Connection dbc, String tablename) throws SQLException;

	/**
	 *	Writes a blob to the requested record.
	 *  @parameter is	The stream to write to the blob. If this is null then the
	 *  				field is set to dbnull.
	 */
	abstract protected void setBlob(Connection dbc, String table, String column, String where, InputStream is, int len) throws SQLException;

	/*--------------------------------------------------------------*/
	/*	CODING:	Helpers..											*/
	/*--------------------------------------------------------------*/

	static public int streamCopy(OutputStream os, InputStream is) throws IOException {
		byte[] buf = new byte[32768];
		int tsz = 0;

		//-- Now move data, using a buffer,
		for(;;) {
			int nr = is.read(buf);
			if(nr == 32768)
				os.write(buf);
			else if(nr > 0)
				os.write(buf, 0, nr);
			else
				break;
			tsz += nr;
		}
		return tsz;
	}
}
