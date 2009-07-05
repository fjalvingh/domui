package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.util.*;

/**
 * The SimpleCollectionModel is a very basic model that directly represents a collection.
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

	public void onShelve() throws Exception {
	// TODO Auto-generated method stub

	}

	public void onUnshelve() throws Exception {
		fireModelChanged();
	}

	//	@SuppressWarnings("unchecked")
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
