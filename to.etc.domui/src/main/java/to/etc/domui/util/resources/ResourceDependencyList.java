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
final public class ResourceDependencyList implements IResourceDependencyList {
	/**
	 * If you do not want to collect dependencies you can use this as an instance. It ignores
	 * everything added to it.
	 */
	static public final IResourceDependencyList NULL = new IResourceDependencyList() {
		@Override
		public void add(@Nonnull IModifyableResource c) {}

		@Override
		public void add(@Nonnull IIsModified m) {}

		@Override
		public void add(@Nonnull IResourceRef ref) {}
	};

	@Nonnull
	private List<IIsModified> m_deplist = Collections.EMPTY_LIST;

	/**
	 * @see to.etc.domui.util.resources.IResourceDependencyList#add(to.etc.domui.util.resources.IResourceRef)
	 */
	@Override
	public void add(@Nonnull IResourceRef ref) {
		if(ref instanceof IIsModified)
			add((IIsModified) ref);
		else if(ref instanceof IModifyableResource)
			add( (IModifyableResource) ref);
		else
			throw new IllegalArgumentException("Argument " + ref + " must implement IIsModified or IModifyableResource to be usable for dependency change tracking");
	}

	/**
	 * @see to.etc.domui.util.resources.IResourceDependencyList#add(to.etc.domui.util.resources.IIsModified)
	 */
	@Override
	public void add(@Nonnull IIsModified m) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<IIsModified>(5);
		else {
			for(IIsModified xm : m_deplist) {
				if(m == xm)
					return;
			}
		}
		m_deplist.add(m);
	}

	/**
	 * @see to.etc.domui.util.resources.IResourceDependencyList#add(to.etc.domui.util.resources.IModifyableResource)
	 */
	@Override
	public void add(@Nonnull IModifyableResource c) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<IIsModified>(5);
		else {
			for(IIsModified xm : m_deplist) {
				if(xm instanceof ResourceTimestamp) {
					ResourceTimestamp rx = (ResourceTimestamp) xm;
					if(rx.getRef() == c)
						return;
				}
			}
		}
		m_deplist.add(new ResourceTimestamp(c, c.getLastModified()));
	}

	/**
	 * @see to.etc.domui.util.resources.IResourceDependencyList#add(to.etc.domui.util.resources.ResourceDependencyList)
	 */
	public void add(@Nonnull ResourceDependencyList c) {
		for(IIsModified mr : c.m_deplist)
			add(mr);
	}

	/**
	 * Get the immutable dependencies instance.
	 * @return
	 */
	public ResourceDependencies createDependencies() {
		return new ResourceDependencies(m_deplist);
	}
}
