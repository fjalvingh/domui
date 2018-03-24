package to.etc.domui.util.asyncdialog;

import to.etc.domui.component.delayed.IAsyncRunnable;
import to.etc.domui.logic.ILogicContext;
import to.etc.domui.logic.LogicContextImpl;
import to.etc.util.FileTool;
import to.etc.util.Progress;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QDataContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
abstract public class AbstractAsyncDialogTask implements IAsyncRunnable {
	@Nullable
	private QDataContext m_dc;

	@Nullable
	private LogicContextImpl m_lc;

	private volatile boolean m_done;

	abstract protected void execute(@Nonnull Progress p) throws Exception;

	@Override
	public final void run(@Nonnull Progress progress) throws Exception {
		try {
			execute(progress);
		} finally {
			m_done = true;
			FileTool.closeAll(m_dc);
			m_dc = null;
			m_lc = null;
		}
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
	protected ILogicContext lc() throws Exception {
		ILogicContext lc = m_lc;
		if(null == lc) {
			lc = m_lc = new LogicContextImpl(dc());
		}
		return lc;
	}

	private boolean isDone() {
		return m_done;
	}
}
