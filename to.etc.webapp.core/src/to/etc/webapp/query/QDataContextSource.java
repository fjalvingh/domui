package to.etc.webapp.query;

import java.util.*;

/**
 * A thingy which knows how to get a QDataContext to access the database. This
 * usually returns a shared context: the one used by the current request.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 25, 2008
 */
public interface QDataContextSource {
	/**
	 * Get the current Session to use for querying.
	 * @return
	 * @throws Exception
	 */
	public QDataContext getDataContext() throws Exception;

	public void releaseDataContext(QDataContext dc);

	/**
	 * Add a new listener for queries for this source. All data sources obtained
	 * from this source will use these listeners.
	 * @param l
	 */
	void	addQueryListener(IQueryListener l);

	/**
	 * Remove an earlier-registered query listener.
	 * @param l
	 */
	void	removeQueryListener(IQueryListener l);

	/**
	 * Returns an iterator over all registered listeners.
	 * @return
	 */
	Iterator<IQueryListener>	getListenerIterator();
}
