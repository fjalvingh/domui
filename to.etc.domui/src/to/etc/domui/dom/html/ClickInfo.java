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
package to.etc.domui.dom.html;

import to.etc.domui.server.*;

/**
 * Extensible info class for a "click" event.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 14, 2011
 */
final public class ClickInfo {
	private boolean m_shift;

	private boolean m_alt;

	private boolean m_control;

	private int m_pageX, m_pageY;

	public ClickInfo(IParameterInfo pi) {
		m_shift = "true".equals(pi.getParameter("_shiftKey"));
		m_control = "true".equals(pi.getParameter("_controlKey"));
		m_alt = "true".equals(pi.getParameter("_altKey"));
		int v;
		try {
			v = Integer.parseInt(pi.getParameter("_pageX"));
		} catch(Exception x) {
			v = 0;
		}
		m_pageX = v;
		try {
			v = Integer.parseInt(pi.getParameter("_pageX"));
		} catch(Exception x) {
			v = 0;
		}
		m_pageY = v;
	}

	public boolean isShift() {
		return m_shift;
	}

	public boolean isAlt() {
		return m_alt;
	}

	public boolean isControl() {
		return m_control;
	}

	public int getPageX() {
		return m_pageX;
	}

	public int getPageY() {
		return m_pageY;
	}
}
