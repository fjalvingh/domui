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

import java.util.*;

public abstract class AbstractTreeNodeBase<T extends ITreeNode<T>> implements ITreeNode<T> {
	private T m_parent;

	private List<T> m_childList;

	abstract public List<T> loadChildren() throws Exception;

	public AbstractTreeNodeBase(T dad) {
		m_parent = dad;
	}

	public void setParent(T parent) {
		m_parent = parent;
	}

	public AbstractTreeNodeBase(T dad, List<T> children) {
		m_parent = dad;
		m_childList = children;
	}

	@Override
	public T getChild(int index) throws Exception {
		if(m_childList == null)
			m_childList = loadChildren();
		return m_childList.get(index);
	}

	@Override
	public int getChildCount() throws Exception {
		if(m_childList == null)
			m_childList = loadChildren();
		return m_childList.size();
	}

	@Override
	public boolean hasChildren() throws Exception {
		if(m_childList == null)
			return true;
		return m_childList.size() != 0;
	}

	@Override
	public T getParent() throws Exception {
		return m_parent;
	}

	protected List<T> getChildList() {
		return m_childList;
	}

	protected void setChildList(List<T> childList) {
		m_childList = childList;
	}
}
