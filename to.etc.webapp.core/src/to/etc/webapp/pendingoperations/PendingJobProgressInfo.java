package to.etc.webapp.pendingoperations;

/**
 * Represent current progress path and percentage of
 * queried pending operation.
 *
 *
 * @author <a href="mailto:jsavic@execom.eu">Jelena Savic</a>
 * Created on Dec 21, 2011
 */
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
