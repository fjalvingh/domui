package to.etc.domui.util.asyncdialog;

import to.etc.domui.component.delayed.AsyncContainer;
import to.etc.domui.component.delayed.IAsyncRunnable;
import to.etc.domui.component.delayed.IProgress;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.function.ConsumerEx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This implements some core required logic to easily do asynchronous code that shows progress
 * inside a dialog.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1/13/15.
 */
final public class AsyncDialog {
	private AsyncDialog() {
	}

	static public <T extends IAsyncTask> void runInDialog(@Nonnull NodeContainer addTo, @Nonnull T task, @Nonnull String dialogTitle, boolean isAbortable, @Nullable ConsumerEx<T> onComplete) {
		runInDialog(addTo, task, dialogTitle, isAbortable, onComplete, null);
	}
	static public <T extends IAsyncTask> void runInDialog(@Nonnull NodeContainer addTo, @Nonnull T task, @Nonnull String dialogTitle, boolean isAbortable, @Nullable ConsumerEx<T> onComplete, @Nullable ConsumerEx<Exception> onError) {
		final Dialog dlg = new Dialog(true, false, dialogTitle);
		addTo.add(dlg);
		dlg.setAutoClose(false);

		AsyncContainer	pd = new AsyncContainer(new IAsyncRunnable() {
			@Override public void run(@Nonnull IProgress p) throws Exception {
				task.execute(p);
			}

			@Override public void onCompleted(boolean cancelled, @Nullable Exception errorException) throws Exception {
				dlg.close();
				if(errorException == null) {
					dlg.close();
					if(null != onComplete)
						onComplete.accept(task);
				} else {
					if(onError == null) {
						MsgBox.error(addTo, errorException.toString());
					} else {
						onError.accept(errorException);
					}
				}
			}
		});
		if (!isAbortable){
			pd.setAbortable(false);
		}
		dlg.add(pd);
	}
}
