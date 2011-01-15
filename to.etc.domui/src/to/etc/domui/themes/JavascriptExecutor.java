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

import java.io.*;

import org.mozilla.javascript.*;

/**
 * Incomplete, unused; retained as POC for later.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 7, 2011
 */
public class JavascriptExecutor {
	//	private final JavascriptExecutorFactory m_factory;

	private Scriptable m_scope;

	public JavascriptExecutor(JavascriptExecutorFactory javascriptExecutorFactory) {
		//		m_factory = javascriptExecutorFactory;
	}

	/**
	 * Create the local scope for this executor, inheriting the root scope containing
	 * Object, Function and other stuff.
	 * @param rootScope
	 */
	public void initialize(ScriptableObject rootScope) {
		Context jcx = Context.enter();
		try {
			m_scope = jcx.newObject(rootScope);
			m_scope.setPrototype(rootScope);
			m_scope.setParentScope(null);
		} finally {
			Context.exit();
		}
	}

	public Object eval(String js) throws Exception {
		Context jcx = Context.enter();
		try {
			return jcx.evaluateString(m_scope, js, "inline", 1, null);
		} finally {
			Context.exit();
		}
	}

	public Object eval(Reader r, String jsname) throws Exception {
		Context jcx = Context.enter();
		try {
			return jcx.evaluateReader(m_scope, r, jsname, 1, null);
		} finally {
			Context.exit();
		}
	}

	public Scriptable getScope() {
		return m_scope;
	}

	public Object toObject(Object o) {
		return Context.toObject(o, m_scope);
	}
}
