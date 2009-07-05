package to.etc.domui.state;

import to.etc.domui.component.delayed.*;

public class DelayedProgressMonitor implements IProgress {
	private DelayedActivitiesManager m_manager;

	private DelayedActivityInfo m_activity;

	private int m_maxWork = -1;

	private int m_currentWork;

	private boolean m_canceled;

	protected DelayedProgressMonitor(DelayedActivitiesManager manager, DelayedActivityInfo activity) {
		m_manager = manager;
		m_activity = activity;
	}

	public void cancel() {
		synchronized(m_manager) {
			m_canceled = true;
		}
	}

	public boolean isCancelled() {
		synchronized(m_manager) {
			return m_canceled;
		}
	}

	public void setCompleted(int work) {
		if(isCancelled())
			throw new DelayedActivityCanceledException();
		if(work > m_currentWork && isReporting()) {
			if(work > m_maxWork)
				work = m_maxWork;
			m_currentWork = work;
			m_manager.completionStateChanged(m_activity, getPercentComplete());
		}
	}

	public void setTotalWork(int work) {
		if(isCancelled())
			throw new DelayedActivityCanceledException();
		m_maxWork = work;
		m_manager.completionStateChanged(m_activity, 0);
	}

	public boolean isReporting() {
		return m_maxWork > 0;
	}

	int getPercentComplete() {
		if(m_maxWork <= 0)
			return 0;
		int pct = 100 * (m_currentWork) / m_maxWork;
		//		System.out.println("%%%% work="+m_currentWork+", max="+m_maxWork+", pct="+pct);
		return pct;
	}
}
