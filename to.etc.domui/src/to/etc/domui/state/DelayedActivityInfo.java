package to.etc.domui.state;

import to.etc.domui.component.delayed.*;
import to.etc.domui.dom.html.*;

public class DelayedActivityInfo {
	private DelayedActivitiesManager m_manager;

	private AsyncContainer m_container;

	private IActivity m_activity;

	private DelayedProgressMonitor m_monitor;

	private Exception m_exception;

	private Div m_executionResult;

	private int m_pctComplete = -1;

	protected DelayedActivityInfo(DelayedActivitiesManager manager, IActivity activity, AsyncContainer ac) {
		m_activity = activity;
		m_manager = manager;
		m_container = ac;
	}

	public IActivity getActivity() {
		return m_activity;
	}

	public DelayedProgressMonitor getMonitor() {
		if(m_monitor == null)
			throw new IllegalStateException("? Unexpected access to monitor after task completed?");
		return m_monitor;
	}

	void setMonitor(DelayedProgressMonitor monitor) {
		m_monitor = monitor;
	}

	public Exception getException() {
		return m_exception;
	}

	void setException(Exception exception) {
		m_exception = exception;
	}

	public Div getExecutionResult() {
		return m_executionResult;
	}

	void setExecutionResult(Div executionResult) {
		m_executionResult = executionResult;
	}

	public void cancel() {
		m_manager.cancelActivity(this);
	}

	int getPercentageComplete() {
		synchronized(m_manager) {
			return m_pctComplete;
		}
	}

	void setPercentageComplete(int pct) {
		m_pctComplete = pct;
	}

	public AsyncContainer getContainer() {
		return m_container;
	}
}
