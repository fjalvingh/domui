package to.etc.parallelrunner;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.util.Progress;

/**
 * This defines a handler for asynchronous execution within a DomUI page. See to.etc.domui.component.delayed.AsyncDiv for a component that uses it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 1, 2012
 */
public interface IAsyncRunnable {
	/**
	 * This is the code to call on the newly created worker thread - it should do the long running task that
	 * you want to execute in parallel. <b>Important:</b> this method will be called on a <i>separate</i> thread, and
	 * it will run while the page it is started from is <b>no longer active at all</b>! This means that this task cannot
	 * access any of that page's resources! This means:
	 * <ul>
	 *	<li>You cannot access the shared QDataContext - if you need one you must call {@link QContextManager#createUnmanagedContext()} or
	 *		something similar to create a context usable within this task - and do not forget to release it either!</li>
	 *	<li>You cannot manipulate any of the originating page's data - no fields, and most certainly no components on that page!!!! To
	 *		show the result of the long running task you must implement {@link #onCompleted(boolean, Exception)}, and use
	 *		properly synchronized(!) member variables in your {@link IAsyncRunnable} implementation to create the new UI to show
	 *		after the task.</li>
	 * </ul>
	 */
	void run(@NonNull Progress p) throws Exception;

	/**
	 * This gets called when the activity is cancelled by the user, <b>on the
	 * thread that caused the cancel, not the executing thread!!</b>. This just
	 * interrupts the executor thread, and hopes either that or the marking of the
	 * progress as cancelled will cancel the task.
	 */
	default void cancel(@NonNull Runnable interruptor) throws Exception {
		interruptor.run();
	}
}
