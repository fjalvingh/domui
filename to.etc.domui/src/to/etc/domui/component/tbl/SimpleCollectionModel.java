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

import to.etc.domui.util.*;

/**
 * DEPRECATED Use SimpleListModel instead. The SimpleCollectionModel is a very basic model that directly represents a collection.
 *
 * <p>20091206 jal This model is deprecated because TableModel's <i>REQUIRE</i> the use of indexed addressing, and this
 * addressing needs to remain stable while using this. This implementation uses Collection and re-creates a list
 * for every time we need indexed addressing. This is VERY expensive, but also unstable when items are added: when
 * they are added to a HashSet for instance the entire internal order can change (after a rehash) causing all items
 * in the table to shift position. Although this model does fire modelChanged so at least the presentation remains
 * correct, this is unacceptable behaviour.</i>
 *
 * The implementation depends on the unshelve command, during unshelving all listeners are notified that the model changed,
 * so changes in the collection this model represents constructor will be reflected in the component that is bound to the model.
 * This means that changes made to the collection itself will be reflected only if that changed are made in a different page then
 * where the component this model is tied to is located. For reflecting changes in the same page the add and remove methods should be
 * called on the model itself, not on the collection it represents.
 *
 * @author Willem
 *
 * @param <T>
 */
@Deprecated
public class SimpleCollectionModel<T> extends TableModelBase<T> implements IShelvedListener {
	private Collection<T> m_collection;

	public SimpleCollectionModel(Collection<T> collection) {
		m_collection = collection;
	}

	protected List<T> getList() throws Exception {
		// TODO Auto-generated method stub
		List<T> list = new ArrayList<T>();
		if(m_collection != null)
			list.addAll(m_collection);
		return list;
	}

	@Override
	public void onShelve() throws Exception {
	// TODO Auto-generated method stub

	}

	@Override
	public void onUnshelve() throws Exception {
		fireModelChanged();
	}

	//	@SuppressWarnings("unchecked")
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
	 * Tries to add an item to the model's collection.
	 * When the collection changed during the operation,
	 * the according listeners will be notified.
	 *
	 * @param item, the item to be added.
	 * @return true if the collection changed, false otherwise.
	 * @throws Exception
	 */
	public boolean add(T item) throws Exception {
		boolean changed = m_collection.add(item);
		if(changed)
			fireModelChanged();
		return changed;
	}

	/**
	 * Tries to remove an item from the model's collection.
	 * When the collection changed during the operation,
	 * the according listeners will be notified.
	 *
	 * @param item, the item to be removed.
	 * @return true if the collection changed, false otherwise.
	 * @throws Exception
	 */
	public boolean remove(T item) throws Exception {
		boolean changed = m_collection.remove(item);
		if(changed)
			fireModelChanged();
		return changed;
	}
}
