package to.etc.log;

import java.util.*;

import javax.annotation.*;

import org.slf4j.*;

import to.etc.log.event.*;

/**
 * {@link Logger} implementation. See {@link EtcLoggerFactory} for more details.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Nov 9, 2012
 */
public class EtcLogger implements Logger {
	@Nonnull
	private final String	m_key;

	@Nullable
	private Level			m_level;

	private EtcLogger(@Nonnull String key, @Nullable Level level) {
		m_key = key;
		m_level = level;
	}

	@Nonnull
	static EtcLogger create(@Nonnull String key, @Nullable Level level) {
		return new EtcLogger(key, level);
	}

	private void logEvent(@Nonnull Date date, @Nonnull Level level, @Nullable Marker marker, @Nullable Throwable throwable, @Nonnull String msg, Object... args) {
		EtcLogEvent event = new EtcLogEvent(this, level, marker, msg, throwable, date, Thread.currentThread(), args);
		EtcLoggerFactory.getSingleton().notifyHandlers(event);
	}

	private synchronized boolean checkEnabled(@Nonnull Level level) {
		return m_level != null && m_level.includes(level);
	}

	private boolean checkEnabled(@Nonnull Level level, @Nonnull Marker marker) {
		//FIXME: in case that we introduce markers support we need to connect it here.
		return checkEnabled(level);
	}

	private void execute(@Nonnull Level level, @Nullable String arg0) {
		if(checkEnabled(level)) {
			logEvent(new Date(), level, null, null, arg0);
		}
	}

	private void execute(@Nonnull Level level, @Nullable String arg0, @Nullable Object arg1) {
		if(checkEnabled(level)) {
			logEvent(new Date(), level, null, null, arg0, arg1);
		}
	}

	private void execute(@Nonnull Level level, @Nullable String arg0, @Nullable Object[] arg1) {
		if(checkEnabled(level)) {
			logEvent(new Date(), level, null, null, arg0, arg1);
		}
	}

	private void execute(@Nonnull Level level, @Nullable String arg0, @Nullable Throwable arg1) {
		if(checkEnabled(level)) {
			logEvent(new Date(), level, null, arg1, arg0);
		}
	}

	private void execute(@Nonnull Level level, @Nullable String arg0, @Nullable Object arg1, @Nullable Object arg2) {
		if(checkEnabled(level)) {
			logEvent(new Date(), level, null, null, arg0, arg1, arg2);
		}
	}

	private void execute(@Nonnull Level level, @Nullable Marker arg0, @Nullable String arg1) {
		if(checkEnabled(level, arg0)) {
			logEvent(new Date(), level, arg0, null, arg1);
		}
	}

	private void execute(@Nonnull Level level, @Nullable Marker arg0, @Nullable String arg1, @Nullable Object arg2) {
		if(checkEnabled(level, arg0)) {
			logEvent(new Date(), level, arg0, null, arg1, arg2);
		}
	}

	private void execute(@Nonnull Level level, @Nullable Marker arg0, @Nullable String arg1, @Nullable Object[] arg2) {
		if(checkEnabled(level, arg0)) {
			logEvent(new Date(), level, arg0, null, arg1, arg2);
		}
	}

	private void execute(@Nonnull Level level, @Nullable Marker arg0, @Nullable String arg1, @Nullable Throwable arg2) {
		if(checkEnabled(level, arg0)) {
			logEvent(new Date(), level, arg0, arg2, arg1);
		}
	}

	private void execute(@Nonnull Level level, @Nullable Marker arg0, @Nullable String arg1, @Nullable Object arg2, @Nullable Object arg3) {
		if(checkEnabled(level, arg0)) {
			logEvent(new Date(), level, arg0, null, arg1, arg2, arg3);
		}
	}

	@Override
	public void debug(String arg0) {
		execute(Level.DEBUG, arg0);
	}

	@Override
	public void debug(String arg0, Object arg1) {
		execute(Level.DEBUG, arg0, arg1);
	}

	@Override
	public void debug(String arg0, Object[] arg1) {
		execute(Level.DEBUG, arg0, arg1);
	}

	@Override
	public void debug(String arg0, Throwable arg1) {
		execute(Level.DEBUG, arg0, arg1);
	}

	@Override
	public void debug(String arg0, Object arg1, Object arg2) {
		execute(Level.DEBUG, arg0, arg1, arg2);
	}

	@Override
	public void debug(Marker arg0, String arg1) {
		execute(Level.DEBUG, arg0, arg1);
	}


	@Override
	public void debug(Marker arg0, String arg1, Object arg2) {
		execute(Level.DEBUG, arg0, arg1, arg2);
	}

	@Override
	public void debug(Marker arg0, String arg1, Object[] arg2) {
		execute(Level.DEBUG, arg0, arg1, arg2);
	}

	@Override
	public void debug(Marker arg0, String arg1, Throwable arg2) {
		execute(Level.DEBUG, arg0, arg1, arg2);
	}

	@Override
	public void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
		execute(Level.DEBUG, arg0, arg1, arg2, arg3);
	}

	@Override
	public void error(String arg0) {
		execute(Level.ERROR, arg0);
	}

	@Override
	public void error(String arg0, Object arg1) {
		execute(Level.ERROR, arg0, arg1);
	}

	@Override
	public void error(String arg0, Object[] arg1) {
		execute(Level.ERROR, arg0, arg1);
	}

	@Override
	public void error(String arg0, Throwable arg1) {
		execute(Level.ERROR, arg0, arg1);
	}

	@Override
	public void error(Marker arg0, String arg1) {
		execute(Level.ERROR, arg0, arg1);
	}

	@Override
	public void error(String arg0, Object arg1, Object arg2) {
		execute(Level.ERROR, arg0, arg1, arg2);
	}

	@Override
	public void error(Marker arg0, String arg1, Object arg2) {
		execute(Level.ERROR, arg0, arg1, arg2);
	}

	@Override
	public void error(Marker arg0, String arg1, Object[] arg2) {
		execute(Level.ERROR, arg0, arg1, arg2);
	}

	@Override
	public void error(Marker arg0, String arg1, Throwable arg2) {
		execute(Level.ERROR, arg0, arg1, arg2);
	}

	@Override
	public void error(Marker arg0, String arg1, Object arg2, Object arg3) {
		execute(Level.ERROR, arg0, arg1, arg2, arg3);
	}

	@Override
	public String getName() {
		return m_key;
	}

	@Override
	public void info(String arg0) {
		execute(Level.INFO, arg0);
	}

	@Override
	public void info(String arg0, Object arg1) {
		execute(Level.INFO, arg0, arg1);
	}

	@Override
	public void info(String arg0, Object[] arg1) {
		execute(Level.INFO, arg0, arg1);
	}

	@Override
	public void info(String arg0, Throwable arg1) {
		execute(Level.INFO, arg0, arg1);
	}

	@Override
	public void info(Marker arg0, String arg1) {
		execute(Level.INFO, arg0, arg1);
	}

	@Override
	public void info(String arg0, Object arg1, Object arg2) {
		execute(Level.INFO, arg0, arg1, arg2);
	}

	@Override
	public void info(Marker arg0, String arg1, Object arg2) {
		execute(Level.INFO, arg0, arg1, arg2);
	}

	@Override
	public void info(Marker arg0, String arg1, Object[] arg2) {
		execute(Level.INFO, arg0, arg1, arg2);
	}

	@Override
	public void info(Marker arg0, String arg1, Throwable arg2) {
		execute(Level.INFO, arg0, arg1, arg2);
	}

	@Override
	public void info(Marker arg0, String arg1, Object arg2, Object arg3) {
		execute(Level.INFO, arg0, arg1, arg2, arg3);
	}

	@Override
	public boolean isDebugEnabled() {
		return checkEnabled(Level.DEBUG);
	}

	@Override
	public boolean isDebugEnabled(Marker arg0) {
		return checkEnabled(Level.DEBUG, arg0);
	}

	@Override
	public boolean isErrorEnabled() {
		return checkEnabled(Level.ERROR);
	}

	@Override
	public boolean isErrorEnabled(Marker arg0) {
		return checkEnabled(Level.ERROR, arg0);
	}

	@Override
	public boolean isInfoEnabled() {
		return checkEnabled(Level.INFO);
	}

	@Override
	public boolean isInfoEnabled(Marker arg0) {
		return checkEnabled(Level.INFO, arg0);
	}

	@Override
	public boolean isTraceEnabled() {
		return checkEnabled(Level.TRACE);
	}

	@Override
	public boolean isTraceEnabled(Marker arg0) {
		return checkEnabled(Level.TRACE, arg0);
	}

	@Override
	public boolean isWarnEnabled() {
		return checkEnabled(Level.WARN);
	}

	@Override
	public boolean isWarnEnabled(Marker arg0) {
		return checkEnabled(Level.WARN, arg0);
	}

	@Override
	public void trace(String arg0) {
		execute(Level.TRACE, arg0);
	}

	@Override
	public void trace(String arg0, Object arg1) {
		execute(Level.TRACE, arg0, arg1);
	}

	@Override
	public void trace(String arg0, Object[] arg1) {
		execute(Level.TRACE, arg0, arg1);
	}

	@Override
	public void trace(String arg0, Throwable arg1) {
		execute(Level.TRACE, arg0, arg1);
	}

	@Override
	public void trace(Marker arg0, String arg1) {
		execute(Level.TRACE, arg0, arg1);
	}

	@Override
	public void trace(String arg0, Object arg1, Object arg2) {
		execute(Level.TRACE, arg0, arg1, arg2);
	}

	@Override
	public void trace(Marker arg0, String arg1, Object arg2) {
		execute(Level.TRACE, arg0, arg1, arg2);
	}

	@Override
	public void trace(Marker arg0, String arg1, Object[] arg2) {
		execute(Level.TRACE, arg0, arg1, arg2);
	}

	@Override
	public void trace(Marker arg0, String arg1, Throwable arg2) {
		execute(Level.TRACE, arg0, arg1, arg2);
	}

	@Override
	public void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
		execute(Level.TRACE, arg0, arg1, arg2, arg3);
	}

	@Override
	public void warn(String arg0) {
		execute(Level.WARN, arg0);
	}

	@Override
	public void warn(String arg0, Object arg1) {
		execute(Level.WARN, arg0, arg1);
	}

	@Override
	public void warn(String arg0, Object[] arg1) {
		execute(Level.WARN, arg0, arg1);
	}

	@Override
	public void warn(String arg0, Throwable arg1) {
		execute(Level.WARN, arg0, arg1);
	}

	@Override
	public void warn(Marker arg0, String arg1) {
		execute(Level.WARN, arg0, arg1);
	}

	@Override
	public void warn(String arg0, Object arg1, Object arg2) {
		execute(Level.WARN, arg0, arg1, arg2);
	}

	@Override
	public void warn(Marker arg0, String arg1, Object arg2) {
		execute(Level.WARN, arg0, arg1, arg2);
	}

	@Override
	public void warn(Marker arg0, String arg1, Object[] arg2) {
		execute(Level.WARN, arg0, arg1, arg2);
	}

	@Override
	public void warn(Marker arg0, String arg1, Throwable arg2) {
		execute(Level.WARN, arg0, arg1, arg2);
	}

	@Override
	public void warn(Marker arg0, String arg1, Object arg2, Object arg3) {
		execute(Level.WARN, arg0, arg1, arg2, arg3);
	}

	synchronized boolean isDisabled() {
		return m_level == null;
	}

	public synchronized void setLevel(@Nullable Level level) {
		m_level = level;
	}
}
