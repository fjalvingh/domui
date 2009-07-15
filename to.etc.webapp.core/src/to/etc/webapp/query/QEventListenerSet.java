package to.etc.webapp.query;

import java.util.*;

/**
 * Used to separate event registration from the data source. An instance of
 * this can be shared by multiple QDataContext factories so that they all share
 * the same registered event handlers.
 * 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
public class QEventListenerSet {
	private List<IQueryListener>		m_listeners = Collections.EMPTY_LIST;

	/**
	 * Add a new listener for queries for this source. All data sources obtained
	 * from this source will use these listeners.
	 * @param l
	 */
	synchronized public void addQueryListener(IQueryListener l) {
		if(m_listeners.contains(l))
			return;
		if(m_listeners == Collections.EMPTY_LIST)
			m_listeners = new ArrayList<IQueryListener>(1);
		else
			m_listeners = new ArrayList<IQueryListener>(m_listeners);
		m_listeners.add(l);
	}

	/**
	 * Remove an earlier-registered query listener.
	 * @param l
	 */
	synchronized public void removeQueryListener(IQueryListener l) {
		m_listeners = new ArrayList<IQueryListener>(m_listeners);
		m_listeners.remove(l);
	}

	/**
	 * Return an iterator over all registered event listeners.
	 * @return
	 */
	synchronized public Iterator<IQueryListener>	getListenerIterator() {
		return m_listeners.iterator();
	}
}
