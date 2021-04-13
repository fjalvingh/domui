package to.etc.domui.util.asyncdialog;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.delayed.AsyncContainer;
import to.etc.domui.component.layout.IWindowClosed;
import to.etc.domui.component.misc.ExceptionDialog;
import to.etc.parallelrunner.IAsyncCompletionListener;
import to.etc.parallelrunner.IAsyncRunnable;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.trouble.UIException;
import to.etc.function.ConsumerEx;
import to.etc.util.CancelledException;

import java.util.Objects;

import static to.etc.domui.component.layout.FloatingDiv.RSN_CLOSE;

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

	static public <T extends IAsyncRunnable> void runInDialog(@NonNull NodeContainer addTo, @NonNull T task, @NonNull String dialogTitle, boolean isAbortable, @Nullable ConsumerEx<T> onComplete) {
		runInDialog(addTo, task, dialogTitle, isAbortable, onComplete, null);
	}
	static public <T extends IAsyncRunnable> void runInDialog(@NonNull NodeContainer addTo, @NonNull T task, @NonNull String dialogTitle, boolean isAbortable, @Nullable ConsumerEx<T> onComplete, @Nullable ConsumerEx<Exception> onError) {
		final Dialog dlg = new Dialog(true, false, dialogTitle) {
			@Override
			public void closePressed() throws Exception {
				IWindowClosed onClose = getOnClose();
				if(null != onClose) {
					onClose.closed(RSN_CLOSE);
				}else {
					super.closePressed();
				}
			}
		};

		dlg.setClosable(isAbortable);
		addTo.add(dlg);
		dlg.setAutoClose(false);

		IAsyncCompletionListener result = new IAsyncCompletionListener() {
			@Override public void onCompleted(boolean cancelled, @Nullable Exception errorException) throws Exception {
				dlg.close();
				if(errorException == null && !cancelled) {
					dlg.close();
					if(null != onComplete) {
						try {
							onComplete.accept(task);
						} catch(Exception x) {
							ExceptionDialog.create(addTo, "complete handler failed", x);
						}
					}
				} else {
					if(null == errorException && cancelled) {
						errorException = new CancelledException();
					}
					Exception aErrorException = Objects.requireNonNull(errorException);
					if(onError == null) {
						if(aErrorException instanceof UIException) {
							MsgBox.error(addTo, aErrorException.getMessage());
						} else {
							MsgBox.error(addTo, aErrorException.toString());
							aErrorException.printStackTrace();
						}
					} else {
						onError.accept(aErrorException);
					}
				}
			}
		};

		AsyncContainer pd = new AsyncContainer(task, result);
		pd.setAbortable(isAbortable);
		dlg.add(pd);
		if(isAbortable) {
			dlg.setOnClose(reason -> {
				if(RSN_CLOSE.equals(reason)) {
					pd.doCancel();
				}
			});
		}
	}
}
