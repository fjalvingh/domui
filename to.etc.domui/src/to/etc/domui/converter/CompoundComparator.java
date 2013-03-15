package to.etc.domui.converter;

import java.util.*;

import javax.annotation.*;

/**
 * A list of comparators executed in order.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 15, 2013
 */
final public class CompoundComparator<T> implements Comparator<T> {
	@Nonnull
	final private List<Comparator<T>> m_list;

	final private int m_descending;

	public CompoundComparator(List<Comparator<T>> list, boolean reverse) {
		m_list = list;
		m_descending = reverse ? -1 : 1;
	}

	@Override
	public int compare(T o1, T o2) {
		for(Comparator<T> c : m_list) {
			int res = c.compare(o1, o2);
			if(res != 0)
				return res * m_descending;
		}
		return 0;
	}
}
