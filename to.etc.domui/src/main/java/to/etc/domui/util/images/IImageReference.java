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
package to.etc.domui.util.images;

import java.io.*;

import javax.annotation.*;

/**
 * This is a reference to some individual original image as returned by
 * IImageRetriever. It is a must-close resource.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 30, 2009
 */
public interface IImageReference extends Closeable {
	@Override void close() throws IOException;

	/**
	 * If this retriever accesses resources that can change after use this must return some usable
	 * indication of the version, usually a "last date changed" timestamp. This value should remain
	 * unchanged over invocations if the object accessed has not changed. It should return -1 if
	 * the source object has been deleted; it should return 0 if the timestamp does not matter.
	 * This gets called multiple times; it should be fast.
	 *
	 * @return
	 */
	long getVersionLong() throws Exception;

	/**
	 * This must return the image's actual mime type.
	 * @return
	 * @throws Exception
	 */
	@Nonnull String getMimeType() throws Exception;

	/**
	 * Returns the datastream containing this image. This may be called only ONCE for an image and must
	 * be closed after use.
	 * @return
	 * @throws Exception
	 */
	InputStream getInputStream() throws Exception;

}
