package to.etc.webapp.query;

import java.util.*;

/**
 * A thingy which knows how to get a QDataContext to access the database. This
 * usually returns a shared context: the one used by the current request.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 25, 2008
 */
public interface QDataContextFactory {
	/**
	 * Get the current Session to use for querying.
	 * @return
	 * @throws Exception
	 */
	QDataContext getDataContext() throws Exception;

	void closeDataContext(QDataContext dc);

	//	void releaseDataContext(QDataContext dc);

	/**
	 * Returns an iterator over all registered listeners.
	 * @return
	 */
	Iterator<IQueryListener>	getListenerIterator();
}
