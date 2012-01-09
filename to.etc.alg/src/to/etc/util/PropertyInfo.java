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

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

/**
 * Information on properties on a class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 9, 2007
 */
@Immutable
final public class PropertyInfo implements IPropertyAccessor {
	final private String	m_name;

	final private Method	m_getter;

	final private Method	m_setter;

	public PropertyInfo(String name, Method getter, Method setter) {
		if(getter == null || name == null)
			throw new IllegalStateException("Name or getter null not allowed");
		m_name = name;
		m_getter = getter;
		m_setter = setter;
	}

	@Nonnull
	public String getName() {
		return m_name;
	}

	@Nonnull
	public Method getGetter() {
		return m_getter;
	}

	@Nullable
	public Method getSetter() {
		return m_setter;
	}

	@Nonnull
	public Class< ? > getActualType() {
		return m_getter.getReturnType();
	}

	public Type getActualGenericType() {
		return m_getter.getGenericReturnType();
	}

	/**
	 * Returns T if this is either a collection or an array.
	 * @return
	 */
	public boolean isCollectionOrArrayType() {
		return getActualType().isArray() || Collection.class.isAssignableFrom(getActualType());
	}

	public boolean isCollectionType() {
		return Collection.class.isAssignableFrom(getActualType());
	}

	public boolean isArrayType() {
		return getActualType().isArray();
	}

	public Class< ? > getCollectionValueType() {
		if(getActualType().isArray())
			return getActualType().getComponentType();
		else if(Collection.class.isAssignableFrom(getActualType())) {
			Type t = getActualGenericType();
			if(t == null)
				return null;
			return ClassUtil.findCollectionType(t);
		} else
			return null;
	}

	@Nullable
	public Object getValue(@Nullable Object instance) throws Exception {
		if(null == m_getter)
			throw new IllegalAccessException("The property " + this + " does not have a getter method - it is writeonly");
		try {
			return m_getter.invoke(instance);
		} catch(InvocationTargetException xte) {
			throw WrappedException.unwrap(xte);
		}
	}

	public void setValue(@Nullable Object instance, @Nullable Object value) throws Exception {
		if(null == m_setter)
			throw new IllegalAccessException("The property " + this + " does not have a getter method - it is writeonly");
		try {
			m_setter.invoke(instance, value);
		} catch(InvocationTargetException xte) {
			throw WrappedException.unwrap(xte);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		Class< ? > clz = m_getter != null ? m_getter.getDeclaringClass() : m_setter.getDeclaringClass();
		sb.append("property ").append(m_name).append(" in class ").append(clz.getName());
		return sb.toString();
	}
}
