package to.etc.domui.util;

import java.util.*;

import to.etc.domui.util.*;
import to.etc.webapp.qsql.*;

/**
 * Basic implementation for collectors that are collecting ILongIdentifyable data.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 23 Dec 2011
 */
public class DataCollector<T extends ILongIdentifyable> {

	protected List<T> m_collected = Collections.EMPTY_LIST;

	protected void collect(List<T> collected) {
		m_collected = DomUtil.merge(m_collected, collected);
	}

	protected void collect(T item) {
		m_collected = DomUtil.merge(m_collected, item);
	}

	public List<T> getCollected() {
		return m_collected;
	}

}
