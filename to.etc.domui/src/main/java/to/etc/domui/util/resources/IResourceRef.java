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
package to.etc.domui.util.resources;

import javax.annotation.*;
import java.io.*;

/**
 * A reference to some stream resource which can be read to create something else, and which is
 * changeable somehow. This gets used where generated resources need to be regenerated if one of
 * their dependencies have changed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
public interface IResourceRef {
	IResourceRef NONEXISTENT = new IResourceRef() {
		@Override public boolean exists() {
			return false;
		}

		@Nullable @Override public InputStream getInputStream() throws Exception {
			return null;
		}
	};

	/**
	 * Return T if this resource actually exists.
	 * @return
	 */
	boolean exists();

	/**
	 * Returns the input stream for the resource. This will return a new stream for every call. It returns null
	 * if the resource does not exist.
	 * @return
	 * @throws Exception
	 */
	@Nullable
	InputStream getInputStream() throws Exception;
}
