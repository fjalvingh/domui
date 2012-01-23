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
package to.etc.util;

import java.util.*;
import javax.annotation.concurrent.*;

/**
 * Cached information on class properties detected in ClassUtil.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 24, 2009
 */
@Immutable
final public class ClassInfo {
	private Class< ? >	m_theClass;

	private List<PropertyInfo>	m_properties;

	public ClassInfo(Class< ? > theClass, List<PropertyInfo> prop) {
		m_theClass = theClass;
		Collections.sort(prop, new Comparator<PropertyInfo>() {
			@Override
			public int compare(PropertyInfo a, PropertyInfo b) {
				return a.getName().compareTo(b.getName());
			}
		});
		m_properties = Collections.unmodifiableList(prop);
	}

	public Class< ? > getTheClass() {
		return m_theClass;
	}

	public List<PropertyInfo> getProperties() {
		return m_properties;
	}

	static private final Comparator<Object>	C_COMP	= new Comparator<Object>() {
		@Override
		public int compare(Object a, Object b) {
			if(a instanceof PropertyInfo) {
				return ((PropertyInfo) a).getName().compareTo((String) b);
			} else {
				return ((PropertyInfo) b).getName().compareTo((String) a);
			}
		}
	};

	public PropertyInfo	findProperty(String name) {
		int ix = Collections.binarySearch(m_properties, name, C_COMP);
		if(ix < 0)
			return null;
		return m_properties.get(ix);
	}

}
