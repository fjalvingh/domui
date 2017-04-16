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
package to.etc.domui.util.bugs;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * Accessor to post odd conditions for later review.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
public class Bug {
	static private final ThreadLocal<IBugListener> m_listener = new ThreadLocal<IBugListener>();

	private Bug() {}

	/**
	 * Show a bug using the specified message.
	 * @param message
	 */
	static public void bug(@Nonnull String message) {
		BugItem	bi = new BugItem(message);
		postBug(bi);
	}

	static public void bug(List<NodeBase> msg) {
		BugItem bi = new BugItem(msg);
		postBug(bi);
	}

	static public void bug(@Nullable Throwable x, @Nonnull String message) {
		BugItem bi = new BugItem(message, x);
		postBug(bi);
	}

	static public void bug(@Nonnull Throwable x) {
		BugItem bi = new BugItem(x.getMessage(), x);
		postBug(bi);
	}

	private static void postBug(BugItem bi) {
		IBugListener	listener = m_listener.get();
		if(null == listener) {
			System.out.println("BUG: " + bi.getMessage());
			Throwable x = bi.getException();
			if(null != x) {
				x.printStackTrace();
			}
			return;
		}

		//-- Post the bug, but do not fail if posting throws booboo
		try {
			listener.bugSignaled(bi);
		} catch(Exception x) {
			x.printStackTrace(); // Explicitly do not handle
		}
	}

	@Nullable
	static public IBugListener getListener() {
		return m_listener.get();
	}

	static public void setListener(@Nullable IBugListener l) {
		m_listener.set(l);
	}
}
