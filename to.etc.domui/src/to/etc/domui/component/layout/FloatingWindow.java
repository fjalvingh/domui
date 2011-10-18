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
package to.etc.domui.component.layout;

import to.etc.domui.dom.html.*;

/**
 * DO NOT USE - Use {@link Window} or {@link Dialog} instead!!
 * A simple floating window, non-blocking, with a title bar which can be dragged. This also
 * acts as an error fence, limiting all errors generated within this control to be displayed
 * within this window.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2008
 */
@Deprecated
public class FloatingWindow extends Window {
	private static final int WIDTH = 640;

	private static final int HEIGHT = 400;

	protected FloatingWindow() {
		super(true, false, WIDTH, HEIGHT, null);
	}

	/**
	 * Create a floating window with the specified title in the title bar.
	 * @param txt
	 */
	protected FloatingWindow(boolean modal, String txt) {
		super(modal, false, WIDTH, HEIGHT, txt);
	}

	/**
	 * Create and link a modal floating window.
	 * @return
	 */
	static public FloatingWindow create(NodeBase parent) {
		return create(parent, null, true);
	}

	static public FloatingWindow create(NodeBase parent, String ttl) {
		return create(parent, ttl, true);
	}

	static public FloatingWindow create(NodeBase parent, String ttl, boolean modal) {
		UrlPage body = parent.getPage().getBody();
		FloatingWindow w = new FloatingWindow(modal, ttl); // Create instance
		body.add(w);
		return w;
	}
}
