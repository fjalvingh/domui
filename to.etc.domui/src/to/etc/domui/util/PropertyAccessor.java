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
package to.etc.domui.util;

import to.etc.domui.component.meta.*;

import java.beans.*;
import java.lang.reflect.*;

/**
 * Should be unused. Pending removal.
 *
 * Generalized access to a property's value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
@Deprecated
final public class PropertyAccessor<T> implements IValueAccessor<T> {
	private PropertyMetaModel<T> m_pmm;
	private Method m_readm;

	private Method m_writem;

	public PropertyAccessor(Method getmethod, Method setmethod, PropertyMetaModel<T> pmm) {
		m_readm = getmethod;
		m_writem = setmethod;
		m_pmm = pmm;
	}

	public PropertyAccessor(PropertyDescriptor pd, PropertyMetaModel<T> pmm) {
		this(pd.getReadMethod(), pd.getWriteMethod(), pmm);
	}

	@Override
	public void setValue(Object target, T value) throws Exception {
		if(target == null)
			throw new IllegalStateException("The 'target' object is null");
		if(m_writem == null)
			throw new IllegalAccessError("The property " + m_pmm + " is read-only.");
		try {
			m_writem.invoke(target, value);
		} catch(InvocationTargetException itx) {
			Throwable c = itx.getCause();
			if(c instanceof Exception)
				throw (Exception) c;
			else if(c instanceof Error)
				throw (Error) c;
			else
				throw itx;
		}
	}

	/**
	 * Retrieve the value from this object. If the input object is null
	 * this throws NPE.
	 *
	 * @see to.etc.domui.util.IValueTransformer#getValue(java.lang.Object)
	 */
	@Override
	public T getValue(Object in) throws Exception {
		if(in == null)
			throw new IllegalStateException("The 'input' object is null (getter method=" + m_readm + ")");
		try {
			return (T) m_readm.invoke(in);
		} catch(InvocationTargetException itx) {
			System.err.println("(in calling " + m_readm + " with input object " + in + ")");
			Throwable c = itx.getCause();
			if(c instanceof Exception)
				throw (Exception) c;
			else if(c instanceof Error)
				throw (Error) c;
			else
				throw itx;
		} catch(Exception x) {
			System.err.println("in calling " + m_readm + " with input object " + in);
			throw x;
		}
	}

	@Override public boolean isReadOnly() {
		return m_writem != null;
	}
}
