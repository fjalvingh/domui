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
package to.etc.domui.themes;

import to.etc.domui.util.resources.*;

/**
 * This implements IIsModified to make a theme part of a dependency list. Because a theme
 * can use a lot of files (all fragments and properties etc) we check for changes only every
 * few seconds. This implements that behaviour.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 10, 2011
 */
final public class ThemeModifiableResource implements IIsModified {
	/** The dependency list that is checked only every [m_interval] milliseconds. */
	final private ResourceDependencies m_wrappedDependencies;

	final private int m_interval;

	/** Timestamp when next test needs to take place. */
	private long m_tsNextTest;

	/** When set, the dependencies have actually changed. */
	private boolean m_modified;

	/**
	 * Create a wrapper.
	 * @param rdl		The resource list to wrap
	 * @param interval	The max age of the test, in milliseconds.
	 */
	public ThemeModifiableResource(final ResourceDependencies rdl, int interval) {
		m_wrappedDependencies = rdl;
		m_interval = interval;
	}

	/**
	 * If it's not yet time to check for changes again return false (unchanged), else
	 * check the original dependencies. If that did not change reset the timer.
	 *
	 * @see to.etc.domui.util.resources.IIsModified#isModified()
	 */
	@Override
	public boolean isModified() {
		synchronized(this) {
			if(m_modified)
				return true;

			long cts = System.currentTimeMillis();
			if(cts < m_tsNextTest)
				return false;

			//-- It's time to use the wrapped thing.
			m_tsNextTest = cts + m_interval;
			if(m_wrappedDependencies.isModified()) {
				//-- Ohh yes.. It has changed allright. Mark this as changed, and stop any further checking.
				m_modified = true;
			}
			return m_modified;
		}
	}
}
