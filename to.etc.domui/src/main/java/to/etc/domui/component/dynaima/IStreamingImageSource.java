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
package to.etc.domui.component.dynaima;

import java.io.*;

/**
 * UNSTABLE INTERFACE
 * Represents an image source for an image that is present
 * as an encoded byte stream, like present on a file system
 * or present in a database BLOB.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public interface IStreamingImageSource {
	/**
	 * This MUST return the stream's mime type, which must be one of the supported formats (jpeg, gif, png)
	 * @return
	 */
	String getMimeType();

	/**
	 * Return the size in bytes of the stream. If the size is unknown return -1.
	 * @return
	 */
	int getSize() throws Exception;

	/**
	 * This must return the stream to use for this resource.
	 * @return
	 * @throws Exception
	 */
	InputStream getInputStream() throws Exception;

	/**
	 * This will be called when resources are to be released.
	 */
	void close();
}
