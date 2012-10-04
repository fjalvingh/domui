package to.etc.domui.component.delayed;

import javax.annotation.*;

/**
 * Listener interface for asynchronous UI actions. Used to collect and restore context information
 * for asynchonous calls.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 28, 2012
 */
public interface IAsyncListener<T> {
	/**
	 * Called when the activity is scheduled. This call can collect any kind of "context" information
	 * needed and store it into some object that is returned. Later calls can then use this "context"
	 * info to properly set context for the asynchonous job.
	 * @param runnable
	 * @return
	 * @throws Exception
	 */
	@Nullable
	T onActivityScheduled(@Nonnull IAsyncRunnable runnable) throws Exception;

	void onActivityStart(@Nonnull IAsyncRunnable runnable, @Nullable T contextData) throws Exception;

	void onActivityEnd(@Nonnull IAsyncRunnable runnable, @Nullable T contextData) throws Exception;
}
