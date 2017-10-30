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


import to.etc.domui.server.reloader.Reloader;

/**
 * Holds the last-modified timestamp for some source "file" used in some production at the time
 * it was used; plus a reference to that file so it's /original/ change time can be determined.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
final public class ResourceTimestamp implements IIsModified {
	private IModifyableResource m_ref;

	private long m_ts;

	public ResourceTimestamp(IModifyableResource ref, long ts) {
		m_ref = ref;
		m_ts = ts;
	}

	@Override
	public boolean isModified() {
		try {
			boolean b = m_ref.getLastModified() != m_ts;
			if(Reloader.DEBUG)
				System.out.println(m_ref + ": " + (b ? " changed" : "unchanged"));
			return b;
		} catch(Exception x) {
			return true;
		}
	}

	public IModifyableResource getRef() {
		return m_ref;
	}

	@Override
	public String toString() {
		return m_ref.toString();
	}
}
