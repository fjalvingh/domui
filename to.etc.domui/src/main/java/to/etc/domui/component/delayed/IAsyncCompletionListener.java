package to.etc.domui.component.delayed;

import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-2-18.
 */
public interface IAsyncCompletionListener {
	/**
	 * This method gets called with your originating DomUI page "active" when the activity has completed. At the time
	 * of call you can manipulate any kind of structure within your page. This method is to be used to handle response
	 * rendering only; it cannot be used to release any kind of resource(!) because it will not be called if your page
	 * terminated (was shelved/unshelved) before the task finished.
	 *
	 * @param cancelled			When TRUE, the task was cancelled by the user.
	 * @param errorException	When non-null the run(Progress) method terminated with an exception, and this is
	 * 							that exception. <b>You are supposed to do something with it</b> since no other indication
	 * 							of that error is reported to the user!! So throw this exception on, or display it on your page.
	 * @throws Exception
	 */
	void onCompleted(boolean cancelled, @Nullable Exception errorException) throws Exception;
}
