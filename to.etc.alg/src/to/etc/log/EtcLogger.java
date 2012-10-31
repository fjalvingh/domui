package to.etc.log;

import java.util.*;

import javax.annotation.*;

import org.slf4j.*;

import to.etc.log.event.*;
import to.etc.log.handler.*;

public class EtcLogger implements Logger {
	
	private final String	m_key;

	private Level							m_level				= null;

	private final Object					m_levelLock			= new Object();
	
	private final List<ILogHandler> m_handlers; 

	private EtcLogger(@Nonnull String key, @Nullable Level level, @Nonnull List<ILogHandler> handlers) {
		m_key = key;
		m_level = level;
		m_handlers = handlers;
	}

	static EtcLogger create(@Nonnull String key, @Nullable Level level, @Nonnull List<ILogHandler> handlers) {
		return new EtcLogger(key, level, handlers);
	}
	
	private void logEvent(@Nonnull Date date, @Nonnull Level level, @Nullable Marker marker, @Nonnull String msg, Object... args){
		LogEvent event = new LogEvent(this, level, marker, msg, null, date, Thread.currentThread(), args);
		for (ILogHandler handler : m_handlers){
			handler.handle(event);
		}
	}

	private boolean checkEnabled(@Nonnull Level level) {
		return !isDisabled() && m_level.includes(level);
	}

	private boolean checkEnabled(@Nonnull Level level, @Nonnull Marker marker) {
		//FIXME: in case that we introduce markers support we need to connect it here.  
		return checkEnabled(level);
	}

	private void execute(Level level, String arg0) {
		if(checkEnabled(level)) {
			logEvent(new Date(), level, null, arg0);
		}
	}

	private void execute(Level level, String arg0, Object arg1) {
		if(checkEnabled(level)) {
			logEvent(new Date(), level, null, arg0, arg1);
		}
	}

	public void execute(Level level, String arg0, Object[] arg1) {
		if(checkEnabled(level)) {
			logEvent(new Date(), level, null, arg0, arg1);
		}
	}

	public void execute(Level level, String arg0, Throwable arg1) {
		if(checkEnabled(level)) {
			logEvent(new Date(), level, null, arg0, arg1);
		}
	}

	public void execute(Level level, String arg0, Object arg1, Object arg2) {
		if(checkEnabled(level)) {
			logEvent(new Date(), level, null, arg0, arg1, arg2);
		}
	}

	public void execute(Level level, Marker arg0, String arg1) {
		if(checkEnabled(level, arg0)) {
			logEvent(new Date(), level, arg0, arg1);
		}
	}

	public void execute(Level level, Marker arg0, String arg1, Object arg2) {
		if(checkEnabled(level, arg0)) {
			logEvent(new Date(), level, arg0, arg1, arg2);
		}
	}

	public void execute(Level level, Marker arg0, String arg1, Object[] arg2) {
		if(checkEnabled(level, arg0)) {
			logEvent(new Date(), level, arg0, arg1, arg2);
		}
	}

	public void execute(Level level, Marker arg0, String arg1, Throwable arg2) {
		if(checkEnabled(level, arg0)) {
			logEvent(new Date(), level, arg0, arg1, arg2);
		}
	}

	public void execute(Level level, Marker arg0, String arg1, Object arg2, Object arg3) {
		if(checkEnabled(level, arg0)) {
			logEvent(new Date(), level, arg0, arg1, arg2, arg3);
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

	boolean isDisabled() {
		return m_level == null;
	}

	public void setLevel(Level level) {
		m_level = level;
	}
}
