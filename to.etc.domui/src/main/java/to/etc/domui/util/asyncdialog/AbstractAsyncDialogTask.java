package to.etc.domui.util.asyncdialog;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.parallelrunner.IAsyncRunnable;
import to.etc.domui.logic.ILogicContext;
import to.etc.domui.logic.LogicContextImpl;
import to.etc.util.FileTool;
import to.etc.util.Progress;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QDataContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
abstract public class AbstractAsyncDialogTask implements IAsyncRunnable, AutoCloseable {
	@Nullable
	private QDataContext m_dc;

	@Nullable
	private LogicContextImpl m_lc;

	private volatile boolean m_done;

	abstract protected void execute(@NonNull Progress p) throws Exception;

	@Override
	public final void run(@NonNull Progress progress) throws Exception {
		try {
			execute(progress);
		} finally {
			m_done = true;
			FileTool.closeAll(m_dc);
			m_dc = null;
			m_lc = null;
		}
	}

	@NonNull
	protected QDataContext	dc() throws Exception {
		QDataContext dc = m_dc;
		if(null == dc) {
			if(isDone())
				throw new IllegalStateException("You cannot use the task context after completion. Use the page's instead");
			dc = m_dc = QContextManager.createUnmanagedContext();
		}
		return dc;
	}

	@NonNull
	protected ILogicContext lc() throws Exception {
		ILogicContext lc = m_lc;
		if(null == lc) {
			lc = m_lc = new LogicContextImpl(dc());
		}
		return lc;
	}

	@Override
	public void close() throws Exception {
		QDataContext dc = m_dc;
		if(dc != null) {
			m_dc = null;
			dc.close();
		}
	}

	private boolean isDone() {
		return m_done;
	}
}
