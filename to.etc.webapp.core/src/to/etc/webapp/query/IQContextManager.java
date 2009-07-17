package to.etc.webapp.query;

/**
 * Interface for a QContextManager handler.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 17, 2009
 */
public interface IQContextManager {
	/**
	 * Initialize the data context factory that is to be used by default to allocate QDataContexts. Can be called
	 * only once.
	 * @param f
	 */
	void setContextFactory(QDataContextFactory f);

	/**
	 * Return the default QDataContextFactory. This is the root of *all* default connections
	 * allocated through DomUI.
	 *
	 * @return
	 */
	QDataContextFactory getDataContextFactory();

	/**
	 * Create an unmanaged (manually closed) context factory.
	 * @return
	 * @throws Exception
	 */
	QDataContext createUnmanagedContext() throws Exception;

	/**
	 * Get/create a shared context factory. The context factory gets attached
	 * to the container it is shared in, and will always try to re-use any
	 * QDataContext already present in the container. In addition, all data contexts
	 * allocated thru this mechanism have a disabled close() method, preventing
	 * them from closing the shared connection.
	 *
	 * @param cc
	 * @return
	 */
	QDataContextFactory getSharedContextFactory(IQContextContainer cc);

	/**
	 * Gets a shared QDataContext from the container. If it is not already present it
	 * will be allocated, stored in the container for later reuse and returned. The context
	 * is special in that it cannot be closed() using it's close() call - it is silently
	 * ignored.
	 */
	QDataContext getSharedContext(IQContextContainer cc) throws Exception;

	/**
	 * If the specified container contains a shared context close it.
	 * @param cc
	 */
	void closeSharedContext(IQContextContainer cc);
}
