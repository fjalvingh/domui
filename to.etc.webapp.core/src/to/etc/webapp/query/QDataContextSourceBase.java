package to.etc.webapp.query;

import java.util.*;

/**
 * Base class for a data source, properly implementing the code to handle listener registration and use.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
abstract public class QDataContextSourceBase implements QDataContextSource {
	private List<IQueryListener>		m_listeners = Collections.EMPTY_LIST;

	abstract public QDataContext getDataContext() throws Exception;
	abstract public void releaseDataContext(QDataContext dc);

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextSource#addQueryListener(to.etc.webapp.query.IQueryListener)
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
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextSource#removeQueryListener(to.etc.webapp.query.IQueryListener)
	 */
	synchronized public void removeQueryListener(IQueryListener l) {
		m_listeners = new ArrayList<IQueryListener>(m_listeners);
		m_listeners.remove(l);
	}

	/**
	 * {@inheritDoc}
	 * @return
	 */
	synchronized public Iterator<IQueryListener>	getListenerIterator() {
		return m_listeners.iterator();
	}
}
