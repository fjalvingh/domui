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
package to.etc.el;

import java.lang.reflect.*;
import java.util.*;

import javax.servlet.jsp.el.*;

/**
 * A function mapper implementation.
 *
 *
 * @author jal
 * Created on Aug 4, 2005
 */
public class FunctionMapperImpl implements FunctionMapper {
	private FunctionMapper m_root;

	private Map<String, Method> m_unnamed_map = new Hashtable<String, Method>();

	private Map<String, Map<String, Method>> m_namedMap = new Hashtable<String, Map<String, Method>>();

	public FunctionMapperImpl() {}

	public FunctionMapperImpl(FunctionMapper root) {
		m_root = root;
	}

	public Method resolveFunction(String pkg, String name) {
		Method m = resolveFunctionHere(pkg, name);
		if(m != null)
			return m;
		if(m_root != null)
			m = m_root.resolveFunction(pkg, name);
		return m;
	}

	public Method resolveFunctionHere(String pkg, String name) {
		if(pkg == null)
			return m_unnamed_map.get(name);
		Map<String, Method> map = m_namedMap.get(pkg);
		if(map == null)
			return null;
		return map.get(name);
	}

	public void add(String name, Method m) {
		if(null != m_unnamed_map.put(name, m))
			throw new IllegalStateException("Duplicate function name '" + name + "' in root package");
	}

	public void add(String pkg, String name, Method m) {
		Map<String, Method> map = m_namedMap.get(pkg);
		if(map == null) {
			map = new Hashtable<String, Method>();
			m_namedMap.put(pkg, map);
		}
		if(null != map.put(name, m))
			throw new IllegalStateException("Duplicate function name '" + pkg + ":" + name + "'");
	}

	/**
	 * Add all static public methods in the class.
	 * @param pkg
	 * @param cl
	 */
	public void add(String pkg, Class cl) {
		Method[] mar = cl.getMethods();
		for(Method m : mar) {
			int mod = m.getModifiers();
			if(Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
				add(pkg, m.getName(), m);
			}
		}
	}

}
