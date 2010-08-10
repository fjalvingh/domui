package to.etc.webapp.pendingoperations;

import to.etc.util.*;

/**
 * Factory type which generates an executor instance
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 6, 2009
 */
public interface IPendingOperationExecutor {
	void executePendingOperation(final PendingOperation po, final ILogSink ls) throws Exception;
}
