package to.etc.log.event;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Marker;
import to.etc.log.EtcLogger;
import to.etc.log.Level;

import java.util.Date;

/**
 * Encapsulates properties of single log event.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class EtcLogEvent {
	@NonNull
	private final EtcLogger	m_logger;

	@NonNull
	private final Level		m_level;

	@Nullable
	private final String	m_msg;

	@Nullable
	private final Throwable	m_thrown;

	@NonNull
	private final Date		m_timestamp;

	@NonNull
	private final Thread	m_thread;

	@Nullable
	private final Object[]	m_args;

	@Nullable
	private final Marker	m_marker;

	public EtcLogEvent(@NonNull EtcLogger logger, @NonNull Level level, @Nullable Marker marker, @Nullable String msg, @Nullable Throwable thrown, @NonNull Date timestamp, @NonNull Thread thread,
		Object... args) {
		m_logger = logger;
		m_level = level;
		m_marker = marker;
		m_msg = msg;
		m_thrown = thrown;
		m_timestamp = timestamp;
		m_thread = thread;
		m_args = args;
	}

	@NonNull
	public EtcLogger getLogger() {
		return m_logger;
	}

	@NonNull
	public Level getLevel() {
		return m_level;
	}

	@Nullable
	public Marker getMarker() {
		return m_marker;
	}

	@Nullable
	public String getMsg() {
		return m_msg;
	}

	@Nullable
	public Throwable getThrown() {
		return m_thrown;
	}

	@NonNull
	public Date getTimestamp() {
		return m_timestamp;
	}

	@NonNull
	public Thread getThread() {
		return m_thread;
	}

	@Nullable
	public Object[] getArgs() {
		return m_args;
	}
}
