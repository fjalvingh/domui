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
package to.etc.webapp.eventmanager;

/**
 * The type of listening operation requested; used to specify when
 * the listener is to be called after an event has occured.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 30, 2006
 */
public enum ListenerType {
	/** A Delayed listener gets called only after an event is posted at the time that the event is read by the separate event thread. */
	DELAYED

	/**
	 * An immediate event is passed to all listeners at the time that the postEvent call is done. This means that
	 * on the server where the event occurs the event is also handled. This should be used when the code that fires
	 * the event also requires the event's effects to be visible after handling the event. Examples: an edit screen
	 * fires an event, then expects the list screen to show the changes due to the event.
	 * <b>Important</b>: Please ensure that your event handling does not contain expensive (slow) actions,
	 * as it directly influences the user's code!
	 */
	, IMMEDIATELY

	/**
	 * The event is only fired on this-server. All other servers in the cluster do not see it(!). This should only
	 * be used in exceptional circumstances.
	 */
	, LOCALLY,
}
