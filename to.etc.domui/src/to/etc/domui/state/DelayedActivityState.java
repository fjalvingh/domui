package to.etc.domui.state;

import java.util.*;

import to.etc.domui.component.delayed.*;

public class DelayedActivityState {
	private List<Progress>		m_progressList;

	private List<DelayedActivityInfo>	m_completionList;

	public static class Progress {
		private AsyncContainer			m_container;
		private int						m_pctComplete;
		private String					m_message;
		protected Progress(AsyncContainer container, int pctComplete, String message) {
			m_container = container;
			m_pctComplete = pctComplete;
			m_message = message;
		}
		public AsyncContainer getContainer() {
			return m_container;
		}
		public int getPctComplete() {
			return m_pctComplete;
		}
		public String getMessage() {
			return m_message;
		}
	}

	protected DelayedActivityState(List<Progress> progressList, List<DelayedActivityInfo> completionList) {
		m_progressList = progressList;
		m_completionList = completionList;
	}

	public List<Progress> getProgressList() {
		return m_progressList;
	}

	public List<DelayedActivityInfo> getCompletionList() {
		return m_completionList;
	}
}
