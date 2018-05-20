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

import java.util.*;

/**
 * This is a comparator which compares objects using a property
 * expression.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 26, 2006
 */
public class PropertySorter implements Comparator<Object> {
	private PropertyExpression m_px;

	private boolean m_desc;

	public PropertySorter(PropertyExpression px, boolean desc) {
		m_px = px;
		m_desc = desc;
	}

	public int compare(Object o1, Object o2) {
		if(m_desc)
			return compareSub(o2, o1);
		else
			return compareSub(o1, o2);
	}

	private int compareSub(Object o1, Object o2) {
		//-- Calculate both values
		try {
			Object a = m_px.getValue(o1, null);
			Object b = m_px.getValue(o2, null);
			if(a == null && b == null)
				return 0;
			if(a == null)
				return -1;
			if(b == null)
				return 1;
			if(a instanceof Comparable && b instanceof Comparable) {
				return ((Comparable) a).compareTo(b);
			}

			throw new IllegalStateException("Do not know how to compare a " + a.getClass().getCanonicalName() + " and a " + b.getClass().getCanonicalName());
		} catch(Exception x) {
			throw new RuntimeException(x);
		}
	}
}
