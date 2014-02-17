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

import javax.annotation.*;

/**
 * Concrete implementation of a tree node model using AbstractTreeNodeBase thingies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 21, 2008
 */
public class TreeNodeModelBase<T extends ITreeNode<T>> implements ITreeModel<T> {
	private T m_root;

	private List<ITreeModelChangedListener<T>> m_listeners = Collections.EMPTY_LIST;

	public TreeNodeModelBase(T root) {
		m_root = root;
	}

	@Override
	public void addChangeListener(@Nonnull ITreeModelChangedListener<T> l) {
		if(m_listeners == Collections.EMPTY_LIST) {
			m_listeners = new ArrayList<ITreeModelChangedListener<T>>();
		}
		if(m_listeners.contains(l))
			return;
		m_listeners.add(l);
	}

	@Override
	public void removeChangeListener(@Nonnull ITreeModelChangedListener<T> l) {
		m_listeners.remove(l);
	}

	protected List<ITreeModelChangedListener<T>> getListeners() {
		return m_listeners;
	}

	@Nonnull
	@Override
	public T getChild(@Nullable T parent, int index) throws Exception {
		if(null == parent)
			throw new IllegalArgumentException("Parent cannot be null");
		return parent.getChild(index);
	}

	@Override
	public int getChildCount(@Nullable T item) throws Exception {
		if(null == item)
			throw new IllegalArgumentException("Item cannot be null");
		return item.getChildCount();
	}

	@Nullable
	@Override
	public T getParent(@Nullable T child) throws Exception {
		if(null == child)
			return null;
		return child.getParent();
	}

	@Override
	@Nullable
	public T getRoot() throws Exception {
		return m_root;
	}

	@Override
	public boolean hasChildren(@Nullable T item) throws Exception {
		if(null == item)
			throw new IllegalArgumentException("Item cannot be null");
		return item.hasChildren();
	}

	@Override
	public void expandChildren(@Nullable T item) throws Exception {
	}

	@Override
	public void collapseChildren(@Nullable T item) throws Exception {
	}
}
