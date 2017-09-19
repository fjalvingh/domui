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
package to.etc.iocular.ioccontainer;

import to.etc.iocular.def.*;

/**
 * Encapsulates the initialization and termination state of the static
 * part of a component. It is kept within the container and gets used
 * to lock the initialization and termination of the static portion
 * of objects.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 26, 2007
 */
public class StaticComponentRef {
	private boolean m_initialized;

	private boolean m_initializing;

	private ComponentDef m_def;

	public StaticComponentRef(ComponentDef def) {
		m_def = def;
	}

	/**
	 * Obtains ownership of this thingy, or waits for ownership to be released.
	 * @return
	 */
	synchronized boolean mustInitialize(BasicContainer c) {
		if(m_initialized) // Already initialized?
			return false; // Be done then
		int tries = 0;
		for(;;) {
			if(m_initialized) // Already initialized?
				return false; // Be done then
			if(!m_initializing) { // No Initialize in progress?
				m_initializing = true; // I'm initializing now: own this
				return true;
			}

			//-- Someone else is trying to initialize... Wait for him to complete and die if it takes too long.
			try {
				wait(20000);
			} catch(InterruptedException x) {}
			if(tries++ > 2)
				throw new IocContainerException(c, "Timeout waiting for 'static' initialization of " + m_def);
		}
	}

	synchronized void initCompleted(boolean success) {
		if(!m_initializing || m_initialized)
			throw new IllegalStateException("!?");
		m_initializing = false;
		m_initialized = success;
		notifyAll();
	}
}
