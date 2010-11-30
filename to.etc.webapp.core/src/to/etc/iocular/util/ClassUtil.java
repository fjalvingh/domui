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
package to.etc.iocular.util;

import java.lang.reflect.*;
import java.util.*;

public class ClassUtil {
	private ClassUtil() {}

	static public <T> Class<T> byName(Class<T> cls, String name) {
		try {
			Class< ? > c = Class.forName(name);
			if(cls.isAssignableFrom(c))
				return (Class<T>) c;
			throw new IllegalStateException("The class '" + name + "' is not of type " + cls);
		} catch(ClassNotFoundException x) {
			x.printStackTrace();
			return null;
		}
	}

	static public <T> T instanceByName(Class<T> cls, String name) {
		try {
			Class< ? > c = Class.forName(name);
			if(!cls.isAssignableFrom(c))
				throw new IllegalStateException("The class '" + name + "' is not of type " + cls);
			return ((Class<T>) c).newInstance();
		} catch(Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	static public <T> Method[] findMethod(Class<T> cls, String method) {
		List<Method> res = new ArrayList<Method>();
		Method[] mar = cls.getMethods();
		for(Method m : mar) {
			if(m.getName().equals(method))
				res.add(m);
		}
		return res.toArray(new Method[res.size()]);
	}

}
