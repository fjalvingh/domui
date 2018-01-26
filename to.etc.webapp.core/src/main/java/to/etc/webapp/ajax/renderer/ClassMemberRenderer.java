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
package to.etc.webapp.ajax.renderer;

import to.etc.util.StringTool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassMemberRenderer {
	static public final String INVALID = "(invalid)";

	/** The method to call on the object to retrieve it's value */
	private final Method m_method;

	/** The name of the property represented by this value, as obtained from the method name. */
	private final String m_name;

	ClassMemberRenderer(final Method m, final String name) {
		m_method = m;
		m_name = name;
	}

	public Method getMethod() {
		return m_method;
	}

	public String getName() {
		return m_name;
	}

	public Object getMemberValue(final Object val) throws RenderMethodException {
		try {
			return m_method.invoke(val, (Object[]) null); // Call the getter
		} catch(InvocationTargetException x) {
			Exception nx = x;
			if(x.getCause() instanceof Exception) {
				nx = (Exception) x.getCause();
			}
			throw new RenderMethodException(m_method, "Class member getter call '" + m_method.toString() + "' failed with " + StringTool.getExceptionMessage(nx), nx);
		} catch(Exception x) {
			throw new RenderMethodException(m_method, "Class member getter call '" + m_method.toString() + "' failed with " + StringTool.getExceptionMessage(x), x);
		}
	}

	public int render(final ObjectRenderer or, final Object val, final int count) throws Exception {
		Object retval = getMemberValue(val);
		//		System.out.println("renderthingy: get "+m_name+" returned "+retval);
		if(retval == INVALID)
			return count;
		if(or.isKnownObject(retval))
			return count;
		or.renderObjectBeforeItem(count, val, m_name, m_method.getReturnType());
		or.renderObjectMember(retval, m_name, m_method.getReturnType());
		or.renderObjectAfterItem(count, val, m_name, m_method.getReturnType());
		return count + 1;
	}
}
