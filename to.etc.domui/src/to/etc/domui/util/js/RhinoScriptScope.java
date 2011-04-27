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

import javax.annotation.*;

import org.mozilla.javascript.*;

/**
 * A Rhino Javascript scope wrapped to allow easy access for common tasks.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 27, 2011
 */
public class RhinoScriptScope implements IScriptScope {
	@Nonnull
	private Scriptable m_scriptable;

	private boolean m_writable;

	public RhinoScriptScope(@Nonnull Scriptable val, boolean writable) {
		m_scriptable = val;
		m_writable = writable;
	}

	public RhinoScriptScope(@Nonnull Scriptable val) {
		this(val, true);
	}

	@Override
	public Object getValue(String name) {
		Object val = m_scriptable.get(name, m_scriptable);
		if(null == val)
			return null;
		if(val instanceof Scriptable) {
			return new RhinoScriptScope((Scriptable) val);
		}

		return val;
	}

	@Override
	public void put(String name, Object instance) {
		if(!m_writable)
			throw new IllegalStateException("This scope is read-only.");
		m_scriptable.put(name, m_scriptable, instance);
	}

	@Override
	public IScriptScope newScope() {
		Context jcx = Context.enter();
		try {
			Scriptable scope = jcx.newObject(m_scriptable);
			scope.setPrototype(m_scriptable);
			scope.setParentScope(null);
			return new RhinoScriptScope(scope, true);
		} finally {
			Context.exit();
		}
	}

	@Override
	public <T> T getAdapter(Class<T> clz) {
		if(clz.isAssignableFrom(Scriptable.class))
			return (T) m_scriptable;
		return null;
	}


}
