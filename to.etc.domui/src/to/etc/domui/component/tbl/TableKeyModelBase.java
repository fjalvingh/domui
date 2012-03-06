package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

/**
 * This model maintains a set of Keys K, and translates those keys to Model items T where needed. This
 * model should be used when maintaining a full set of T instances is too expensive. This model only
 * instantiates those T instances that are needed to display on a table's page. This is done by
 * loading all T's for a set of K's passed to {@link #getItems(List)}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 7, 2011
 */
abstract public class TableKeyModelBase<K, T> extends TableModelBase<T> implements ITableModel<T>, IModifyableTableModel<K> {
	/** The real collection used. This one will be updated by add and delete calls. */
	@Nonnull
	final private Collection<K> m_sourceCollection;

	/** The ordered and indexable copy of the real collection. If the real collection is a List itself this will be that instance, else a copy will be made. */
	@Nonnull
	final private List<K> m_keyList;

	/** When set this becomes an ordered model. Because it needs to sort on K the key might need extra data for the sort to become meaningful. */
	@Nullable
	private Comparator<K> m_comparator;

	/**
	 * This method must return, for every K in the keys parameter, the proper T that belongs to that K, in the same
	 * order as the K's specified. If calculating a T for a K is very expensive the implementation may decide to
	 * cache the mapping for a certain number of K's. The model itself does not cache any T's returned by it.
	 *
	 * @param keys
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	abstract protected List<T> getItems(List<K> keys) throws Exception;

	/**
	 * Create an unsortable model using the specified collection as source. This collection will be modified if
	 * one of the {@link IModifyableTableModel} methods is called.
	 * @param keycoll
	 */
	public TableKeyModelBase(@Nonnull Collection<K> keycoll) {
		m_sourceCollection = keycoll;
		m_keyList = keycoll instanceof List< ? > ? (List<K>) keycoll : new ArrayList<K>(keycoll);
	}

	/**
	 * Create a model where the <b>keys</b> are sorted using the specified comparator. For this to work
	 * the <b>keys</b> must contain enough information so that they can be sorted. It is explicitly
	 * <b>forbidden</b> to implement this sort as loading the T then sorting on it's fields because this
	 * would instantiate all records in the collection..
	 *
	 * @param keycoll
	 * @param comp
	 */
	public TableKeyModelBase(@Nonnull Collection<K> keycoll, @Nonnull Comparator<K> comp) {
		m_sourceCollection = keycoll;
		m_keyList = new ArrayList<K>(keycoll); // We always need a copy of the list, so that we can sort it
		m_comparator = comp;
		Collections.sort(m_keyList, comp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Nonnull
	public List<T> getItems(int start, int end) throws Exception {
		int size = getRows();
		if(start < 0)
			start = 0;
		if(end > size)
			end = size;
		if(start >= size || end <= 0 || start >= end)
			return Collections.emptyList();
		List<K>	ktodo = m_keyList.subList(start, end);		// All keys we need to get a record for
		List<T>	res = getItems(ktodo);
		return res;
	}

	/**
	 * Internal: get a T for the specified index, to properly send events.
	 *
	 * @see to.etc.domui.component.tbl.TableModelBase#getItem(int)
	 */
	@Override
	@Nullable
	final protected T getItem(int index) throws Exception {
		try {
			List<T> res = getItems(index, index + 1);
			return res.get(0);
		} catch(Exception x) {
			//-- We accept problems here, and pass a null value into the "deleted" thingy..
			return null;
		}
	}

	/**
	 * @see to.etc.domui.component.tbl.ITableModel#getRows()
	 */
	@Override
	public int getRows() throws Exception {
		return m_keyList.size();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IModifyableTableModel<K> implementation.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a new key to show @ the specified location in the list(!). If the
	 * source object is List itself it will be added there at the same location. If
	 * the object is another kind of Collection it will be added using add(). This cannot
	 * be used for models that have a sortable key, because the sort order will determine
	 * the index to use.
	 *
	 * @see to.etc.domui.component.tbl.IModifyableTableModel#add(int, java.lang.Object)
	 * @throws IllegalStateException	when the model is sortable.
	 */
	@Override
	public void add(int index, @Nonnull K key) throws Exception {
		if(m_comparator != null)
			throw new IllegalStateException("Cannot add by index on a sorted model: the sorting order determines the insert index");
		m_keyList.add(index, key);
		if(m_sourceCollection != m_keyList)
			m_sourceCollection.add(key);
		fireAdded(index);
	}

	/**
	 * Add a new key to the model. If the key model itself is sortable then the key is
	 * added at the appropriate location in the backing key list. If the set is not
	 * sorted the key is added at the end of the backing list.
	 * The key is added to the backing Collection too; if that collection is a List itself
	 * the key will be added at the exact same location as in the backing list, otherwise
	 * it gets added to the source model using {@link Collection#add(Object)}.
	 *
	 * @see to.etc.domui.component.tbl.IModifyableTableModel#add(java.lang.Object)
	 */
	@Override
	public void add(@Nonnull K row) throws Exception {
		int index;
		if(m_comparator == null) {
			index = m_keyList.size();
			m_keyList.add(row);
			if(m_keyList != m_sourceCollection)
				m_sourceCollection.add(row);
		} else {
			index = Collections.binarySearch(m_keyList, row, m_comparator);
			if(index < 0)
				index = -(index + 1);
			m_keyList.add(index, row);
			if(m_sourceCollection != m_keyList) {
				if(m_sourceCollection instanceof List< ? >) {
					((List<K>) m_sourceCollection).add(index, row);
				} else {
					m_sourceCollection.add(row);
				}
			}
		}
		fireAdded(index);
	}

	/**
	 * Delete the specified index from the backing list. The key is also removed from
	 * the source collection using the key's value with {@link Collection#remove(Object)}.
	 * @see to.etc.domui.component.tbl.IModifyableTableModel#delete(int)
	 */
	@Override
	@Nullable
	public K delete(int index) throws Exception {
		T deleted = getItem(index);
		K old = m_keyList.remove(index);
		if(m_sourceCollection != m_keyList) {
			m_sourceCollection.remove(old);
		}
		fireDeleted(index, deleted);
		return old;
	}

	/**
	 * Delete the specified key from the list and the backing set.
	 *
	 * @see to.etc.domui.component.tbl.IModifyableTableModel#delete(java.lang.Object)
	 */
	@Override
	public boolean delete(@Nonnull K val) throws Exception {
		int ix = m_keyList.indexOf(val);
		if(ix == -1)
			return false;
		delete(ix);
		return true;
	}

	/**
	 * Send a "modified" message for the specified index.
	 * @param index
	 * @throws Exception
	 */
	public void modified(int index) throws Exception {
		fireModified(index);
	}

	/**
	 * Send a modified event for the specified key, if found.
	 * @param key
	 * @throws Exception
	 */
	public void modified(K key) throws Exception {
		int ix = m_keyList.indexOf(key);
		if(ix != -1)
			fireModified(ix);
	}

	/**
	 * Convenience method to move a key from index <i>from</i> to index <i>to</i>.
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
		K key = delete(from);
		if(null == key)
			return;
		add(to, key);
	}
}
