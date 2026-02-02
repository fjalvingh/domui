/*
 * DomUI Java User Interface - shared code
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
package to.etc.telnet;

/**
 * Known about states for MultiThread-safe objects.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TelnetStateThing {
	static public final int	tsNONE		= 0;

	static public final int	tsRUN		= 1;

	static public final int	tsSHUT		= 2;

	static public final int	tsDOWN		= 3;

	static public final int	tsINITING	= 4;

	/// State of the server..
	private int				m_state;

	public TelnetStateThing() {
		m_state = tsNONE;
	}

	/**
	 *	Returns T if the server is in a given state.
	 */
	public synchronized boolean inState(int st) {
		return m_state == st;
	}

	/**
	 *	Sets the server's state,
	 */
	public synchronized void setState(int st) {
		m_state = st;
	}

	/**
	 *	Returns the server's state.
	 */
	public synchronized int getState() {
		return m_state;
	}

	public String getStateString() {
		switch(m_state){
			default:
				return "?? Unknown";
			case tsNONE:
				return "none";
			case tsRUN:
				return "running";
			case tsSHUT:
				return "being shut down";
			case tsDOWN:
				return "down";
			case tsINITING:
				return "initializing";
		}
	}

}
