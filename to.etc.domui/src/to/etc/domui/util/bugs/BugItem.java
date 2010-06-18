package to.etc.domui.util.bugs;

import java.util.*;

import javax.annotation.*;

/**
 * A single message reported through the bugs thing.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
final public class BugItem {
	final private Date m_timestamp = new Date();

	@Nonnull
	private String m_message;

	@Nullable
	private Throwable m_exception;

	@Nonnull
	private Exception m_location;

	private int m_number;

	public BugItem(@Nonnull String message) {
		m_message = message;
		initLocation();
	}

	public BugItem(@Nonnull String message, @Nullable Throwable exception) {
		m_message = message;
		m_exception = exception;
		initLocation();
	}

	private void initLocation() {
		try {
			throw new Exception("duh");
		} catch(Exception x) {
			m_location = x;
		}
	}

	@Nonnull
	public Date getTimestamp() {
		return m_timestamp;
	}

	@Nonnull
	public String getMessage() {
		return m_message;
	}

	@Nullable
	public Throwable getException() {
		return m_exception;
	}

	@Nonnull
	public Exception getLocation() {
		return m_location;
	}

	public int getNumber() {
		return m_number;
	}

	public void setNumber(int number) {
		m_number = number;
	}
}
