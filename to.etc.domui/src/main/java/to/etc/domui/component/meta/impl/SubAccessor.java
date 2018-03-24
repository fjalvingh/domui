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
package to.etc.domui.component.meta.impl;

import to.etc.domui.util.*;

public class SubAccessor<B, V> implements IValueAccessor<V> {
	/** This accessor provides us with the ROOT value to use, */
	private IValueAccessor<B> m_rootAccessor;

	/** This accessor then uses the root value and transforms it in the property's value on that object. */
	private IValueAccessor<V> m_valueAccessor;

	public SubAccessor(IValueAccessor<B> rootAccessor, IValueAccessor<V> valueAccessor) {
		m_rootAccessor = rootAccessor;
		m_valueAccessor = valueAccessor;
	}

	@Override
	public V getValue(Object in) throws Exception {
		Object root = m_rootAccessor.getValue(in);
		if(root == null)
			return null;
		//InstanceRefresher.refresh(in);
		return m_valueAccessor.getValue(root);
	}

	@Override
	public void setValue(Object target, V value) throws Exception {
		Object root = m_rootAccessor.getValue(target);
		if(root == null)
			throw new IllegalStateException("The value is null: cannot reach a relational object.");
		m_valueAccessor.setValue(root, value);
	}

	@Override public boolean isReadOnly() {
		return m_valueAccessor.isReadOnly();
	}
}
