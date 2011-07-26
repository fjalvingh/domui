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
package to.etc.domui.server.parts;

import javax.annotation.concurrent.*;

import to.etc.domui.util.resources.*;

/**
 * Contains a cached instance of some part rendering.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 4, 2008
 */
@Immutable
final public class CachedPart {
	private final byte[][] m_data;

	final int m_size;

	final ResourceDependencies m_dependencies;

	private final String m_contentType;

	/** The time a response may be cached locally, in seconds */
	final int m_cacheTime;

	final Object m_extra;

	public CachedPart(byte[][] data, int size, int cacheTime, String contentType, ResourceDependencies dependencies, Object extra) {
		m_data = data;
		m_size = size;
		m_cacheTime = cacheTime;
		m_contentType = contentType;
		m_dependencies = dependencies;
		m_extra = extra;
	}

	public byte[][] getData() {
		return m_data;
	}

	public int getSize() {
		return m_size;
	}

	public ResourceDependencies getDependencies() {
		return m_dependencies;
	}

	public String getContentType() {
		return m_contentType;
	}

	public int getCacheTime() {
		return m_cacheTime;
	}

	public Object getExtra() {
		return m_extra;
	}
}