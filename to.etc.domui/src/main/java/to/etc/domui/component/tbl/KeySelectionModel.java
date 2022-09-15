package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.function.FunctionEx;
import to.etc.util.WrappedException;
import to.etc.webapp.query.IIdentifyable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Example implementation of a simple selection model, retaining only the instances key.
 *
 * @param <T>	The instance type
 * @param <K>	The key type
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 15, 2011
 */
public class KeySelectionModel<T, K> extends AbstractSelectionModel<T> {
	final private Map<K, T> m_selectedSet = new HashMap<K, T>();

	final private boolean m_multiSelect;

	final private boolean m_retainInstances;

	final private FunctionEx<T, K> m_keyExtractor;

	/**
	 * Constructor.
	 *
	 * @param retainInstances Set T in case that model should collect instances. For lightweight use, set F in case that collecting PKs is sufficient.
	 */
	public KeySelectionModel(boolean multiSelect, boolean retainInstances) {
		this(multiSelect, retainInstances, rowinstance -> {
			if(rowinstance instanceof IIdentifyable<?>) {
				return ((IIdentifyable<K>) rowinstance).getId();
			} else if(null == rowinstance) 							// Should not happen.
				throw new IllegalArgumentException("null row");
			throw new IllegalStateException("The instance needs to implement IIdentifyable<K>, or you need to set a KeyExtractor");
		});
	}

	public KeySelectionModel(boolean multiSelect, boolean retainInstances, FunctionEx<T, K> keyExtractor) {
		m_multiSelect = multiSelect;
		m_retainInstances = retainInstances;
		m_keyExtractor = keyExtractor;
	}

	public KeySelectionModel(boolean multiSelect) {
		this(multiSelect, false);
	}

	public KeySelectionModel(boolean multiSelect, FunctionEx<T, K> keyExtractor) {
		this(multiSelect, false, keyExtractor);
	}

	@Override
	public boolean isMultiSelect() {
		return m_multiSelect;
	}

	@Override
	public boolean isSelected(@NonNull T rowinstance) {
		if(null == rowinstance) // Should not happen.
			throw new IllegalArgumentException("null row");
		return m_selectedSet.containsKey(getKey(rowinstance));
	}

	/**
	 * This must return the unique key K for an instance. By default, if the instance implements
	 * {@link IIdentifyable}&lt;K&gt; it will use that to retrieve the key; if that is not possible
	 * you <b>must</b> override this method.
	 */
	public K getKey(@NonNull T rowinstance) {
		try {
			return m_keyExtractor.apply(rowinstance);
		} catch(Exception x) {
			throw WrappedException.wrap(x);						// 8-(
		}
	}

	@Override
	public int getSelectionCount() {
		return m_selectedSet.size();
	}

	@Override
	public void setInstanceSelected(@NonNull T rowinstance, boolean on) throws Exception {
		if(null == rowinstance) // Should not happen.
			throw new IllegalArgumentException("null row");

		K key = getKey(rowinstance);
		if(on) {
			if(!m_multiSelect && m_selectedSet.size() > 0) {
				//-- We need to remove an earlier selected item.
				T old = m_selectedSet.values().iterator().next();
				if(rowinstance.equals(old))
					return;
				m_selectedSet.clear();
				callChanged(old, false);
			}
			if(m_selectedSet.containsKey(key)) // Already selected?
				return;
			m_selectedSet.put(key, m_retainInstances ? rowinstance : null);
			callChanged(rowinstance, true);
		} else {
			if(!m_selectedSet.containsKey(key)) // Already unselected?
				return;
			m_selectedSet.remove(getKey(rowinstance));
			callChanged(rowinstance, false);
		}
	}

	@Override
	public void clearSelection() throws Exception {
		if(m_selectedSet.size() == 0)
			return;
		m_selectedSet.clear();
		callSelectionAllChanged();
	}

	@Override
	public void selectAll(ITableModel<T> in) throws Exception {
		int index = 0;
		int rows = in.getRows();
		while(index < rows) {
			int eix = index + 50;
			if(eix > rows)
				eix = rows;
			List<T> itemlist = in.getItems(index, eix);
			for(T item : itemlist) {
				K key = getKey(item);
				m_selectedSet.put(key, m_retainInstances ? item : null);
			}
			index = eix;
		}
		callSelectionAllChanged();
	}

	public List<T> getSelectedInstances() {
		if(!m_retainInstances) {
			throw new IllegalStateException("Selection model is not set to retain instances!");
		}
		return new ArrayList<T>(m_selectedSet.values());
	}

	public Set<K> getSelectedKeys() {
		return new HashSet<K>(m_selectedSet.keySet());
	}
}
