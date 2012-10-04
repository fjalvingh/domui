package to.etc.domui.converter;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.util.*;

/**
 * This comparator compares the <i>values</i> of two properties inside some
 * objects, using the {@link PropertyMetaModel} to define that property, and
 * a user-specific comparator to compare that value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 18, 2012
 */
final public class PropertyComparator<T> implements Comparator<T> {
	@Nonnull
	final private PropertyMetaModel<T> m_pmm;

	@Nonnull
	final private Comparator<T> m_valueComp;

	private boolean m_descending;

	public PropertyComparator(@Nonnull PropertyMetaModel<T> pmm, @Nonnull Comparator<T> comp, boolean descending) {
		m_pmm = pmm;
		m_valueComp = comp;
		m_descending = descending;
	}

	@Override
	public int compare(T o1, T o2) {
		try {
			T a = m_pmm.getValue(o1);
			T b = m_pmm.getValue(o2);
			int res = m_valueComp.compare(a, b);
			return m_descending ? -res : res;
		} catch(Exception x) {
			throw WrappedException.wrap(x); // Checked exception are utter idiocy
		}
	}
}
