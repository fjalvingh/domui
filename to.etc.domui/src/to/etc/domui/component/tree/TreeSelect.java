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
package to.etc.domui.component.tree;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;

public class TreeSelect<T> extends Tree<T> {
	private T m_value;

	public TreeSelect() {}

	public TreeSelect(ITreeModel<T> model) {
		super(model);
	}

	public T getValue() {
		return m_value;
	}

	public void setValue(T value) {
		if(MetaManager.areObjectsEqual(value, m_value))
			return;
		if(value != null && getNodeSelectablePredicate() != null) {
			try {
				if(!getNodeSelectablePredicate().predicate(value))
					throw new IllegalStateException("You cannot the value to a node that is marked as NOT SELECTABLE by the nodeSelectablePredicate");
			} catch(Exception x) {
				;
			}
		}

		if(m_value != null)
			markAsSelected(m_value, false);
		m_value = value;
		if(value != null)
			markAsSelected(value, true);
	}

	@Override
	protected boolean isSelectable(T node) throws Exception {
		if(getNodeSelectablePredicate() == null)
			return true;
		return getNodeSelectablePredicate().predicate(node);
	}

	@Override
	protected void cellClicked(TD cell, T value) throws Exception {
		setValue(value);
		super.cellClicked(cell, value);
	}
}
