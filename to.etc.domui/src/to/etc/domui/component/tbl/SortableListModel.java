package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.util.*;

/**
 * This is a list model where the list instance will be maintained by the model, and where the
 * values can be sorted using property based sorting.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 8, 2011
 */
public class SortableListModel<T> extends SimpleListModel<T> implements ISortableTableModel {
	/** The actual type of the class being listed. */
	private final Class<T> m_dataClass;

	/** The current sort key (a property in the data class), or null if currently unsorted. */
	private String m_sortKey;

	/** T if the sort order is currently descending. */
	private boolean m_descending;

	public SortableListModel(Class<T> clz, List<T> list) {
		super(list);
		m_dataClass = clz;
	}

	/**
	 * Returns the type of the record contained in the list.
	 * @return
	 */
	public Class<T> getDataClass() {
		return m_dataClass;
	}

	@Override
	public void sortOn(String key, boolean descending) throws Exception {
		if(StringTool.isEqual(key, m_sortKey) && m_descending == descending)
			return;

		m_sortKey = key;
		m_descending = descending;
		if(key == null) {
			if(getComparator() != null) {
				setComparator(null);
				fireModelChanged();
			}
			return;
		}

		//-- We need the property meta model for the specified property.
		ClassMetaModel cmm = MetaManager.findClassMeta(getDataClass());

		//		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(getDataClass(), key); // Will abort on bad name
		Comparator<T> comp = (Comparator<T>) ConverterRegistry.getComparator(cmm, key, descending);
		setComparator(comp);
		fireModelChanged();
	}

	@Override
	public String getSortKey() {
		return m_sortKey;
	}

	@Override
	public boolean isSortDescending() {
		return m_descending;
	}
}
