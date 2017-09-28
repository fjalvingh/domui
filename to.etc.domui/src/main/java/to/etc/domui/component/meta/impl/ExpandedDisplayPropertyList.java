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

import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.util.IValueAccessor;

import java.util.List;

/**
 * This is a special version of an expanded property, used when
 * the property referred to consists of multiple properties for
 * display (this is the case when the thingy is part of another
 * class). The instance of this class itself describes the
 * property; it's contents (the list of "child" properties) each
 * describe the content of each child.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2008
 */
public class ExpandedDisplayPropertyList<T> extends ExpandedDisplayProperty<T> {
	private List<ExpandedDisplayProperty< ? >> m_children;

	protected ExpandedDisplayPropertyList(PropertyMetaModel<T> propertyMeta, IValueAccessor<T> accessor, List<ExpandedDisplayProperty< ? >> children) {
		super(propertyMeta.getActualType(), propertyMeta, accessor);
		m_children = children;
	}

	public List<ExpandedDisplayProperty< ? >> getChildren() {
		return m_children;
	}
}
