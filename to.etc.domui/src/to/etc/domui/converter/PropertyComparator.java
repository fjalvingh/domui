package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.util.*;

@SuppressWarnings("rawtypes")
final public class PropertyComparator<T> implements Comparator<T> {
	private PropertyMetaModel< ? > m_pmm;

	private Comparator m_valueComp;

	private boolean m_descending;

	public PropertyComparator(PropertyMetaModel< ? > pmm, Comparator< ? > comp, boolean descending) {
		m_pmm = pmm;
		m_valueComp = comp;
		m_descending = descending;
	}

	public int compare(T o1, T o2) {
		try {
			Object a = m_pmm.getValue(o1);
			Object b = m_pmm.getValue(o2);
			int res = m_valueComp.compare(a, b);
			return m_descending ? -res : res;
		} catch(Exception x) {
			throw WrappedException.wrap(x); // Checked exception are utter idiocy
		}
	}
}
