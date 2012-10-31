package to.etc.log.event;

import java.util.*;

import javax.annotation.*;

import org.slf4j.*;

import to.etc.log.*;

/**
 * Encapsulates properties of single log event. 
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class LogEvent {
	private final EtcLogger	m_logger;

	private final Level		m_level;

	private final String	m_msg;

	private final Throwable	m_thrown;

	private final Date		m_timestamp;

	private final Thread	m_thread;

	private final Object[]	m_args;

	private final Marker	m_marker;

	public EtcLogger getLogger() {
		return m_logger;
	}

	public LogEvent(@Nonnull EtcLogger logger, @Nonnull Level level, @Nullable Marker marker, @Nullable String msg, @Nullable Throwable thrown, @Nonnull Date timestamp, @Nonnull Thread thread,
		Object... args) {
		super();
		m_logger = logger;
		m_level = level;
		m_marker = marker;
		m_msg = msg;
		m_thrown = thrown;
		m_timestamp = timestamp;
		m_thread = thread;
		m_args = args;
	}

	public Level getLevel() {
		return m_level;
	}

	public Marker getMarker() {
		return m_marker;
	}

	public String getMsg() {
		return m_msg;
	}

	public Throwable getThrown() {
		return m_thrown;
	}

	public Date getTimestamp() {
		return m_timestamp;
	}

	public Thread getThread() {
		return m_thread;
	}

	public Object[] getArgs() {
		return m_args;
	}
}
