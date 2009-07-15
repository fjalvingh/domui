package to.etc.webapp.query;

import java.util.*;

/**
 * Base class for a data source, properly implementing the code to handle listener registration and use.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
abstract public class QDataContextFactoryBase implements QDataContextFactory {
	private QEventListenerSet	m_eventSet;

	abstract public QDataContext getDataContext() throws Exception;
	abstract public void releaseDataContext(QDataContext dc);

	public QDataContextFactoryBase(QEventListenerSet eventSet) {
		m_eventSet = eventSet;
	}

	/**
	 * {@inheritDoc}
	 * @return
	 */
	synchronized public Iterator<IQueryListener>	getListenerIterator() {
		return m_eventSet.getListenerIterator();
	}
}
