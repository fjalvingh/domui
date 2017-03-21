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

import java.io.*;

/**
 * Classpath resource for PRODUCTION (non-debug) mode. This refers to a .classpath reference and
 * is initialized only once.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 15, 2010
 */
public class ProductionClassResourceRef implements IResourceRef, IModifyableResource {
	private String m_path;

	private boolean m_exists;

	public ProductionClassResourceRef(String rootpath) {
		m_path = rootpath;
		InputStream is = null;
		try {
			is = getInputStream();
			if(is != null)
				is.close();
		} catch(Exception x) {}
		m_exists = is != null;
	}

	@Override
	public boolean exists() {
		return m_exists;
	}

	@Override
	public InputStream getInputStream() throws Exception {
		return getClass().getResourceAsStream(m_path);
	}

	/**
	 * This one only returns existence: it returns -1 if the resource does
	 * not exist and 1 if it does.
	 * @see to.etc.domui.util.resources.IModifyableResource#getLastModified()
	 */
	@Override
	public long getLastModified() {
		return m_exists ? 1 : -1;
	}

	@Override
	public String toString() {
		return "ProductionClassResourceRef[" + m_path + "]";
	}
}
