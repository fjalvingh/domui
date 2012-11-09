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
public class EtcLogEvent {
	@Nonnull
	private final EtcLogger	m_logger;

	@Nonnull
	private final Level		m_level;

	@Nullable
	private final String	m_msg;

	@Nullable
	private final Throwable	m_thrown;

	@Nonnull
	private final Date		m_timestamp;

	@Nonnull
	private final Thread	m_thread;

	@Nullable
	private final Object[]	m_args;

	@Nullable
	private final Marker	m_marker;

	public EtcLogEvent(@Nonnull EtcLogger logger, @Nonnull Level level, @Nullable Marker marker, @Nullable String msg, @Nullable Throwable thrown, @Nonnull Date timestamp, @Nonnull Thread thread,
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

	@Nonnull
	public EtcLogger getLogger() {
		return m_logger;
	}

	@Nonnull
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

	@Nonnull
	public Date getTimestamp() {
		return m_timestamp;
	}

	@Nonnull
	public Thread getThread() {
		return m_thread;
	}

	@Nullable
	public Object[] getArgs() {
		return m_args;
	}
}
