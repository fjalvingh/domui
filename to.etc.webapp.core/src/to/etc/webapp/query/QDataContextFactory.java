package to.etc.webapp.query;


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

	/**
	 * Returns all event listeners that need to be called for queries executed by contexts generated from here.
	 * @return
	 */
	QEventListenerSet		getEventListeners();

	/**
	 * Returns handlers for all query types.
	 * @return
	 */
	QQueryHandlerList getQueryHandlerList();
}
