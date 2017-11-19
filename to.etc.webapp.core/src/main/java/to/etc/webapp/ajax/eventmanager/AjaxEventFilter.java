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
package to.etc.webapp.ajax.eventmanager;

/**
 * <p>Filters Ajax events for each individual Comet connection/user. When an event
 * is generated all current connections to the comet handler must receive the
 * event.</p>
 *
 * <p>Logically, the event filter's "filterEvent" method gets called for <i>every</i>
 * event when it gets passed to <i>every</i> comet listener. This means that the
 * filter has the ability to change each event for each listener individually.</p>
 *
 * <p>For performance reasons the filters must be able to operate in bulk mode. In this
 * mode the filter initializes for one specific event, then it's filterEvent() method
 * gets called for every listener that needs the event. After this the system calls
 * the filter's close() method allowing it to release any resources it has allocated.</p>
 *
 * <p>When filtering the filter is <i>not</i> allowed to change the input data because that
 * is the single copy of the data that was passed to the postEvent() call. If the filter
 * needs to change data it has to create a copy, change the copy and return that as it's
 * result</p>
 */
public interface AjaxEventFilter {
	/**
	 * Filter the data.
	 *
	 * @param eventCometContext
	 * @return
	 */
	Object filterEvent(EventCometContext eventCometContext, Object eventdata) throws Exception;

	/**
	 * Eventueel opruimen van allerlei zooi.
	 *
	 */
	void close();
}
