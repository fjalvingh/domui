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

import javax.annotation.*;

/**
 * Factory to obtain an image's data from a per-retriever key string.
 *
 * UNSTABLE INTERFACE
 * Thingy which can obtain images from some source (signal interface).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public interface IImageRetriever {
	/**
	 * Returns an unique string identifier, usable in file names and URL's, to represent all images
	 * obtained from this retriever. Called once when the retriever is registered. The value returned
	 * by this call may not change over the lifetime of this factory.
	 * @return
	 */
	@Nonnull String getRetrieverKey();

	/**
	 * Returns the check interval, in millis. This is the age that an image may have in the cache before it's
	 * rechecked for changes again. Returning 0 means the image gets checked for validity always.
	 * @return
	 */
	long getCheckInterval();

	@Nullable IImageReference loadImage(@Nonnull String key) throws Exception;
}
