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
package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

abstract public class TableModelBase<T> implements ITableModel<T> {
	final private List<ITableModelListener<T>> m_listeners = new ArrayList<ITableModelListener<T>>();

	abstract protected T getItem(int ix) throws Exception;

	/**
	 * Add a change listener to this model. Don't forget to remove it at destruction time.
	 */
	@Override
	public void addChangeListener(@Nonnull ITableModelListener<T> l) {
		m_listeners.add(l);
	}

	/**
	 * Remove a change listener from the model.
	 * @see to.etc.domui.component.tbl.ITableModel#removeChangeListener(to.etc.domui.component.tbl.ITableModelListener)
	 */
	@Override
	public void removeChangeListener(@Nonnull ITableModelListener<T> l) {
		m_listeners.remove(l);
	}

	protected List<ITableModelListener<T>> getListeners() {
		return m_listeners;
	}

	public void fireAdded(int index) throws Exception {
		T o = getItem(index);
		for(ITableModelListener<T> l : getListeners())
			l.rowAdded(this, index, o);
	}

	public void fireDeleted(int index, T deleted) throws Exception {
		for(ITableModelListener<T> l : getListeners())
			l.rowDeleted(this, index, deleted);
	}

	public void fireModified(int index) throws Exception {
		T o = getItem(index);
		for(ITableModelListener<T> l : getListeners())
			l.rowModified(this, index, o);
	}

	public void fireModelChanged() throws Exception {
		for(ITableModelListener<T> l : getListeners())
			l.modelChanged(this);
	}

	@Override
	public void refresh() {}
}
