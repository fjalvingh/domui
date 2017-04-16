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

abstract public class TableListModelBase<T> extends TableModelBase<T> implements IModifyableTableModel<T> {
	abstract protected List<T> getList() throws Exception;

	/** When set this becomes an ordered model. */
	private Comparator<T> m_comparator;

	//	/** Indicates that the list has been sorted. */
	//	private boolean m_ordered;

	@Override
	public @Nonnull List<T> getItems(int start, int end) throws Exception {
		int size = getRows();
		if(start < 0)
			start = 0;
		if(end > size)
			end = size;
		if(start >= size || end <= 0 || start >= end)
			return Collections.EMPTY_LIST;
		return getList().subList(start, end);
	}

	@Override
	public T getItem(int index) throws Exception {
		return getList().get(index);
	}

	@Override
	public int getRows() throws Exception {
		return getList().size();
	}

	/**
	 * When set the list will be kept ordered.
	 * @return
	 */
	public Comparator<T> getComparator() {
		return m_comparator;
	}

	/**
	 * Sets a new comparator to use. This resorts the model, if needed, causing a full model update.
	 * @param comparator
	 * @throws Exception
	 */
	public void setComparator(Comparator<T> comparator) throws Exception {
		if(m_comparator == comparator)
			return;
		m_comparator = comparator;
		if(m_comparator != null) {
			resort();
			fireModelChanged();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handling model changes								*/
	/*--------------------------------------------------------------*/

	private void resort() throws Exception {
		Collections.sort(getList(), m_comparator);
		//		m_ordered = true;
	}

	/**
	 * Add the item at the specified index. The item currently at that position
	 * and all items above it move up a notch.
	 */
	@Override
	public void add(int index, @Nonnull T row) throws Exception {
		if(m_comparator != null)
			throw new IllegalStateException("Cannot add by index on a sorted model: the sorting order determines the insert index");
		getList().add(index, row);
		fireAdded(index);
	}

	/**
	 * Add the item at the end (or the appropriate location wrt the sort order) of the list.
	 */
	@Override
	public void add(@Nonnull T row) throws Exception {
		int index;
		if(m_comparator == null) {
			index = getList().size();
			getList().add(row);
		} else {
			index = Collections.binarySearch(getList(), row, m_comparator);
			if(index < 0)
				index = -(index + 1);
			getList().add(index, row);
		}
		fireAdded(index);
	}

	/**
	 * Delete the object at the specified index.
	 * @param index
	 */
	@Override
	public T delete(int index) throws Exception {
		T old = getList().remove(index);
		fireDeleted(index, old);
		return old;
	}

	@Override
	public boolean delete(@Nonnull T val) throws Exception {
		int ix = getList().indexOf(val);
		if(ix == -1)
			return false;
		delete(ix);
		return true;
	}

	public void modified(int index) throws Exception {
		fireModified(index);
	}

	public void modified(@Nonnull T val) throws Exception {
		int ix = getList().indexOf(val);
		if(ix != -1)
			fireModified(ix);
	}

	public int indexOf(@Nonnull T val) throws Exception {
		return getList().indexOf(val);
	}

	/**
	 * Convenience method to move an item from index <i>from</i> to index <i>to</i>.
	 *
	 * @param to
	 * @param from
	 * @throws Exception
	 */
	public void move(int to, int from) throws Exception {
		if(m_comparator != null)
			throw new IllegalStateException("Cannot move objects in a sorted model: the sorting order determines the insert index");
		//-- Sanity checks
		if(to == from)
			throw new IllegalStateException("'from' and 'to' are the same: " + to);
		if(from < 0 || from >= getRows())
			throw new IllegalStateException("Invalid 'from' index (out of bounds): " + from);
		if(to < 0 || to >= getRows())
			throw new IllegalStateException("Invalid 'to' index (out of bounds): " + to);
		T obj = delete(from);
		//		if(to > from)
		//			to--;
		add(to, obj);
	}
}
