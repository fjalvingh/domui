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
package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.util.*;

/**
 * Threadsafe fast listener list implementation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2013
 */
public class ListenerList<V, E extends IChangeEvent<V, E, T>, T extends IChangeListener<V, E, T>> {
	@Nonnull
	static private final Object[] NONE = new Object[0];

	@Nonnull
	private Object[] m_listeners = NONE;

	/**
	 * Add a new listener to the set.
	 * @param listener
	 */
	public synchronized void addChangeListener(@Nonnull T listener) {
		//-- Already exists?
		final int length = m_listeners.length;
		for(int i = length; --i >= 0;) {
			if(m_listeners[i] == listener)
				return;
		}

		//-- We need a change. Reallocate, then add
		Object[] ar = new Object[length + 1];
		System.arraycopy(m_listeners, 0, ar, 0, length);
		ar[length] = listener;
		m_listeners = ar;
	}

	/**
	 * Remove the listener if it exists. This leaves a null hole in the array.
	 * @param listener
	 */
	public synchronized void removeChangeListener(@Nonnull T listener) {
		//-- Already exists?
		final int length = m_listeners.length;
		for(int i = length; --i >= 0;) {
			if(m_listeners[i] == listener) {
				m_listeners[i] = null;
				return;
			}
		}
	}

	@Nonnull
	private synchronized Object[] getListeners() {
		return m_listeners;
	}

	/**
	 * Remove all listeners.
	 */
	public void clear() {
		m_listeners = NONE;
	}

	/**
	 * Call all listeners.
	 * @param event
	 */
	public void fireEvent(@Nonnull E event) {
		try {
			for(Object o : getListeners()) {
				if(null != o) {
					T listener = (T) o;								// Java generics SUCK: arrays cannot properly be generic.
					listener.handleChange(event);
				}
			}
		} catch(Exception x) {
			/*
			 * It's evil but we must wrap here, else all observed objects need throws clauses in their setters. It's a nice
			 * example of how completely and utterly useless checked exceptions are: we will still have an exception, which
			 * no one knows how to handle, but it is now also masked.
			 */
			throw WrappedException.wrap(x);
		}
	}
}
