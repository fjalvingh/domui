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
package to.etc.net;

import java.io.*;

/**
 * Used to provide file data for a file parameter to send to a HTTP server
 * using MultipartPoster. This class has three methods to open, to get data
 * and to close something which is to be send as a file. These methods are
 * called as follows:
 *	<ul><li>open() is called before the data is to be sent</li>
 * 		<li>repeated calls to getBytes() are done until it returns -1; each
 *      	call must copy whatever data fits into the byte array passed</li>
 *      <li>close() is called after this process</li>
 * 	</ul>
 * </p>
 * <p>For an example implementation reading a file see aFileToSend in the
 * MultipartPoster source.</p>
 *
 */
@Deprecated
public abstract class MultipartFile {
	/**
	 * This must provide data for the file to sent. It gets called with a buffer
	 * that should be filled with as much data as will fit. It will be called
	 * repeatedly until it returns -1.
	 * @param buf		the buffer to copy data to,
	 * @return			the #bytes put into the buffer, or -1 when done.
	 * @throws IOException	on any error.
	 */
	public abstract int getBytes(byte[] buf) throws IOException;


	public void open() throws IOException {
	}

	public int getSize() {
		return 0;
	}

	public void close() {
	}
}
