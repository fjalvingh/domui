package to.etc.webapp.pendingoperations;

import javax.annotation.*;

/**
 * This extended version of {@link IPendingOperationExecutor} allows operations that failed in
 * some way to be skipped when part of a group.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 13, 2012
 */
public interface IPendingOperationExecutor2 extends IPendingOperationExecutor {
	/**
	 * Called with a grouped pending operation for this provider that has a FAILED state like FATL or BOOT, this method can
	 * return T if skipping the failed operation and continuing with the group is allowed. By returning false the operation
	 * will remain queued and should be resolved manually before the rest of the group executes.
	 *
	 * @param op
	 * @return
	 */
	boolean isSkipFailedAllowed(@Nonnull PendingOperation op);
}
