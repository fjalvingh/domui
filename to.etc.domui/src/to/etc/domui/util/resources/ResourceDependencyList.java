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
 * Used to build resource dependencies. Dependencies on resources can be
 * added to this list, and when done a ResourceDependencies object can
 * be gotten from this. This is NOT threadsafe(!), but the resulting
 * {@link ResourceDependencies} instance is.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
final public class ResourceDependencyList {
	@Nonnull
	private List<IIsModified> m_deplist = Collections.EMPTY_LIST;

	/**
	 * Add a resource to the dependency list. The resource should either implement {@link IIsModified}
	 * or {@link IModifyableResource}, or this will throw an IllegalArgmentException. If the ref
	 * implements {@link IModifyableResource} then it will be wrapped in a {@link ResourceTimestamp}
	 * instance which records the current modification time and implements {@link IIsModified}.
	 *
	 * @param ref
	 */
	public void add(@Nonnull IResourceRef ref) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<IIsModified>(5);
		if(ref instanceof IIsModified)
			m_deplist.add((IIsModified) ref);
		else if(ref instanceof IModifyableResource) {
			IModifyableResource c = (IModifyableResource) ref;
			m_deplist.add(new ResourceTimestamp(c, c.getLastModified()));
		} else
			throw new IllegalArgumentException("Argument " + ref + " must implement IIsModified or IModifyableResource to be usable for dependency change tracking");
	}

	/**
	 * Add a IIsModified instance.
	 * @param m
	 */
	public void add(@Nonnull IIsModified m) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<IIsModified>(5);
		m_deplist.add(m);
	}

	/**
	 * Add an {@link IModifyableResource} instance.
	 * @param m
	 */
	public void add(@Nonnull IModifyableResource c) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<IIsModified>(5);
		m_deplist.add(new ResourceTimestamp(c, c.getLastModified()));
	}

	/**
	 * Add another list of resources to this one.
	 * @param c
	 */
	public void add(@Nonnull ResourceDependencyList c) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<IIsModified>(5);
		for(IIsModified mr : c.m_deplist)
			m_deplist.add(mr);
	}

	/**
	 * Get the immutable dependencies instance.
	 * @return
	 */
	public ResourceDependencies createDependencies() {
		return new ResourceDependencies(m_deplist);
	}
}
