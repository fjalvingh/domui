package to.etc.domui.util;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 3/8/16.
 */

import to.etc.domui.component.delayed.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.logic.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

import javax.annotation.*;

/**
 * This implements some core required logic to easily do asynchronous code that shows progress
 * inside a dialog.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1/13/15.
 */
abstract public class AsyncDialogTask {
	@Nullable
	private QDataContext m_dc;

	@Nullable
	private LogicContextImpl m_lc;

	private boolean m_done;

	protected abstract void execute(@Nonnull IProgress progress) throws Exception;

	public AsyncDialogTask() {
	}

	final private void run(@Nonnull IProgress p) throws Exception {
		try {
			execute(p);
		} finally {
			FileTool.closeAll(m_dc);
			m_dc = null;
			m_lc = null;
		}
	}

	private synchronized boolean isDone() {
		return m_done;
	}

	private synchronized void done() {
		m_done = true;
	}

	@Nonnull
	protected QDataContext	dc() throws Exception {
		QDataContext dc = m_dc;
		if(null == dc) {
			if(isDone())
				throw new IllegalStateException("You cannot use the task context after completion. Use the page's instead");
			dc = m_dc = QContextManager.createUnmanagedContext();
		}
		return dc;
	}

	@Nonnull
	protected ILogicContext	lc() throws Exception {
		ILogicContext lc = m_lc;
		if(null == lc) {
			lc = m_lc = new LogicContextImpl(dc());
		}
		return lc;
	}

	protected void onCompleted(NodeContainer node) throws Exception {
	}

	protected void onError(Dialog dlg, boolean cancelled, @Nonnull Exception errorException) {
		if(errorException instanceof CodeException) {
			CodeException cx = (CodeException) errorException;
			String msg = cx.getMessage();
			dlg.add(msg);
		} else {
			String msg = errorException.getMessage();
			if(StringTool.isBlank(msg))
				msg = errorException.toString();
			dlg.add(msg);
		}
	}

	static public void runInDialog(@Nonnull NodeContainer addTo, @Nonnull final AsyncDialogTask task, @Nonnull String dialogTitle, final boolean isAbortable, final boolean autoClose) {
		final Dialog dlg = new Dialog(true, false, dialogTitle);
		addTo.add(dlg);
		dlg.setAutoClose(false);

		AsyncContainer	pd = new AsyncContainer(new IAsyncRunnable() {
			@Override public void run(@Nonnull IProgress p) throws Exception {
				task.run(p);
			}

			@Override public void onCompleted(boolean cancelled, @Nullable Exception errorException) throws Exception {
				if(errorException == null) {
					task.onCompleted(addTo);
					if(autoClose) {
						dlg.close();
					}
				} else {
					task.onError(dlg, cancelled, errorException);
				}
				//
				//boolean haserror = false;
				//if(task.isDefault()) {
				//	if(errorException != null) {
				//		haserror = true;
				//		if(errorException instanceof CodeException) {
				//			CodeException cx = (CodeException) errorException;
				//			String msg = cx.getMessage();
				//			dlg.add(msg);
				//		} else {
				//			String msg = errorException.getMessage();
				//			if(StringTool.isBlank(msg))
				//				msg = errorException.toString();
				//			dlg.add(msg);
				//		}
				//	}
				//}
				//
				//if(! haserror) {
				//	if(autoClose) {
				//		dlg.close();
				//	} else {
				//		dlg.add(Msgs.BUNDLE.getString(Msgs.ASYD_COMPLETED));
				//	}
				//}
			}
		});
		if (!isAbortable){
			pd.setAbortable(false);
		}
		dlg.add(pd);
	}
}
