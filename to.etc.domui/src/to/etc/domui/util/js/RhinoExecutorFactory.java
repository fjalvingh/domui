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
package to.etc.domui.util.js;

import org.mozilla.javascript.*;

/**
 * Execute Javascript code, using Rhino. The JDK embedded scripting engine
 * sucks like a Nilfisk: it is a severely abused version of Rhino that is
 * inaccessible by code outside the scripting engine. Consequently it can
 * only be used to do pathetic simple things.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 6, 2011
 */
public class RhinoExecutorFactory {
	static private RhinoExecutorFactory m_instance = new RhinoExecutorFactory();

	/** This is the root Javascript scope, containing things like "Function", "Object" and other fun and games. */
	private ScriptableObject	m_rootScope;

	public static RhinoExecutorFactory getInstance() {
		return m_instance;
	}

	public synchronized void initialize() {
		if(m_rootScope != null)
			return;

		Context jcx = Context.enter();
		try {
			m_rootScope = jcx.initStandardObjects(null, true); // SEAL all standard library object in scope but allow other additions.

			//			// Force the LiveConnect stuff to be loaded.
			//			String loadMe = "RegExp; getClass; java; Packages; JavaAdapter;";
			//			jcx.evaluateString(m_rootScope, loadMe, "lazyLoad", 0, null);
			//			m_rootScope.sealObject();
		} finally {
			Context.exit();
		}
	}

	public RhinoExecutor createExecutor() {
		initialize();

		RhinoExecutor jx = new RhinoExecutor(m_rootScope);
		return jx;
	}


}
