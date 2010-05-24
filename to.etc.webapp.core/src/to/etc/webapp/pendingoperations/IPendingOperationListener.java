package to.etc.webapp.pendingoperations;

/**
 * Interface which can be used to listen to events on the pending operation queue.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 24, 2010
 */
public interface IPendingOperationListener {
	void beforeOperation(PendingOperation po) throws Exception;

	void afterOperation(PendingOperation po, Throwable errx) throws Exception;


}
