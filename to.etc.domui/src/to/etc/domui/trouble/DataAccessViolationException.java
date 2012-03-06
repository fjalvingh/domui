package to.etc.domui.trouble;

/**
 * Use to break access on certain secured data in illegal access context.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 2 Dec 2011
 */
public class DataAccessViolationException extends RuntimeException {
	private String m_details;

	public DataAccessViolationException(String msg, String details) {
		super(msg);
		m_details = details;
	}

	public DataAccessViolationException(String msg) {
		super(msg);
	}

	public String getDetails() {
		return m_details;
	}
}