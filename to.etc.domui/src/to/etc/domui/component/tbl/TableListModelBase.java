package to.etc.domui.component.tbl;

import java.util.*;

abstract public class TableListModelBase<T> extends TableModelBase<T> {
	abstract protected List<T> getList() throws Exception;

	public List<T> getItems(int start, int end) throws Exception {
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

	public int getRows() throws Exception {
		return getList().size();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handling model changes								*/
	/*--------------------------------------------------------------*/

	/**
	 * Add the item at the specified index. The item currently at that position
	 * and all items above it move up a notch.
	 */
	public void add(int index, T row) throws Exception {
		getList().add(index, row);
		fireAdded(index);
	}

	/**
	 * Add the item at the end of the list.
	 */
	public void add(T row) throws Exception {
		int index = getList().size();
		getList().add(row);
		fireAdded(index);
	}

	/**
	 * Delete the object at the specified index.
	 * @param index
	 */
	public T delete(int index) throws Exception {
		T old = getList().remove(index);
		fireDeleted(index, old);
		return old;
	}

	public boolean delete(T val) throws Exception {
		int ix = getList().indexOf(val);
		if(ix == -1)
			return false;
		delete(ix);
		return true;
	}

	public void modified(int index) throws Exception {
		fireModified(index);
	}

	/**
	 * Convenience method to move an item from index <i>from</i> to index <i>to</i>.
	 *
	 * @param to
	 * @param from
	 * @throws Exception
	 */
	public void move(int to, int from) throws Exception {
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
