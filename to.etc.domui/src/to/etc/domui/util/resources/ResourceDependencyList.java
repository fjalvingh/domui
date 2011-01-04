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

import java.util.*;

import javax.annotation.*;

/**
 * Contains a list of things that an "owner" depends on, and for each thing
 * a "timenstamp" of that thing at the time it was used (added) to this list.
 * By comparing the "actual" timestamp with the stored timestamp we can see
 * if the item changed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
final public class ResourceDependencyList {
	@Nonnull
	private List<ResourceTimestamp> m_deplist = Collections.EMPTY_LIST;

	/**
	 * Add a new resource to the list. The resource's timestamp is obtained and stored at this time.
	 * @param c
	 */
	public void add(@Nonnull IModifyableResource c) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<ResourceTimestamp>(5);
		m_deplist.add(new ResourceTimestamp(c, c.getLastModified()));
	}

	/**
	 * Add another list of resources to this one.
	 * @param c
	 */
	public void add(@Nonnull ResourceDependencyList c) {
		for(ResourceTimestamp mr : c.m_deplist)
			m_deplist.add(mr);
	}

	/**
	 * Compares the current timestamp of the resource with the one it had when it was
	 * added, and returns true if any resource has changed  (= has a different timestamp).
	 * @return
	 */
	public boolean isModified() {
		for(ResourceTimestamp c : m_deplist) {
			if(c.isModified())
				return true;
		}
		return false;
	}
}
