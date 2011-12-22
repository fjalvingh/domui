package to.etc.webapp.pendingoperations;

public class PendingJobProgressInfo {

	private final String m_progressPath;

	private final int m_progressPercentage;

	PendingJobProgressInfo(final String progressPath, final int progressPercentage) {
		m_progressPath = progressPath;
		m_progressPercentage = progressPercentage;
	}

	public String getProgressPath() {
		return m_progressPath;
	}

	public int getProgressPercentage() {
		return m_progressPercentage;
	}

}
