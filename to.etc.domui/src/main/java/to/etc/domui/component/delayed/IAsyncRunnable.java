package to.etc.domui.component.delayed;

import to.etc.util.Progress;
import to.etc.webapp.query.QContextManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This defines a handler for asynchronous execution within a DomUI page. See {@link AsyncContainer} for a component that uses it.
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
	 *
	 * @param p
	 * @throws Exception
	 */
	void run(@Nonnull Progress p) throws Exception;
	//
	///**
	// * This method gets called with your originating DomUI page "active" when the activity has completed. At the time
	// * of call you can manipulate any kind of structure within your page. This method is to be used to handle response
	// * rendering only; it cannot be used to release any kind of resource(!) because it will not be called if your page
	// * terminated (was shelved/unshelved) before the task finished.
	// *
	// * @param cancelled			When TRUE, the task was cancelled by the user.
	// * @param errorException	When non-null the {@link #run(Progress)} method terminated with an exception, and this is
	// * 							that exception. <b>You are supposed to do something with it</b> since no other indication
	// * 							of that error is reported to the user!! So throw this exception on, or display it on your page.
	// * @throws Exception
	// */
	//void onCompleted(boolean cancelled, @Nullable Exception errorException) throws Exception;
}
