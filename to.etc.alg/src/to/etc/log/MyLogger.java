package to.etc.log;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.annotation.*;

import org.slf4j.*;

public class MyLogger implements Logger {
	
	public enum Level {
		DEBUG(0), INFO(1), TRACE(2), WARN(3), ERROR(4);
		
		final int m_code;
		
		Level(int code){
			m_code = code;
		}
		
		int getCode(){
			return m_code;
		}
	}
	
	private Map<Marker, Level>		m_enabledMarkers	= new HashMap<Marker, Level>();

	private final String	m_key;

	private String							m_out;
	
	private static final DateFormat			m_tf				= new SimpleDateFormat("HH:mm:ss.SSS");

	private static final DateFormat m_df = new SimpleDateFormat("yyMMdd");

	private boolean m_disabled = false;

	private Level							m_level				= null;

	private final MyLoggerFactory.Config	m_rootConfig;
	
	private final Object					m_writeLock			= new Object();

	private MyLogger(String key, String out, MyLoggerFactory.Config rootConfig) {
		m_key = key;
		m_out = out;
		m_rootConfig = rootConfig;
	}

	static MyLogger create(String key, String out, MyLoggerFactory.Config rootConfig) {
		return new MyLogger(key, out, rootConfig);
	}
	
	private synchronized void logLine(Date date, Level level, Marker marker, String msg, Object... args) {
		StringBuilder sb = new StringBuilder();
		sb.append(m_tf.format(date)).append("\t");
		sb.append(level.name()).append("\t");
		sb.append(Thread.currentThread().getName()).append("\t");
		if (marker != null){
			sb.append("#").append(marker.getName()).append("#").append("\t");
		}
		sb.append("[").append(m_key).append("]").append("\t");
		sb.append(String.format(msg, args));
		String line = sb.toString();
		synchronized(m_writeLock) {
			if(m_out == null) {
				System.out.println(line);
			} else {
				BufferedWriter w = null;
				String fileName = null;
				if(m_out.contains(":")) {
					fileName = m_out;
				} else {
					fileName = m_rootConfig.getLogDir() + File.separator + m_out;
				}

				fileName += "_" + m_df.format(new Date()) + ".log";

				File outFile = new File(fileName);
				outFile.getParentFile().mkdirs();
				try {
					outFile.createNewFile();
					w = new BufferedWriter(new FileWriter(outFile, true));
					w.write(line);
					w.newLine();
				} catch(IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} finally {
					if(w != null) {
						try {
							w.close();
						} catch(IOException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
	}

	private boolean checkEnabled(@Nonnull Level level) {
		return !m_disabled && getLevel().getCode() <= level.getCode();
	}

	private boolean checkEnabled(@Nonnull Level level, @Nonnull Marker marker) {
		if(checkEnabled(level))
			return true;
		synchronized(m_enabledMarkers) {
			Level l = m_enabledMarkers.get(marker);
			return l != null && l.getCode() <= level.getCode();
		}
	}

	private void execute(Level level, String arg0) {
		if(checkEnabled(level)) {
			logLine(new Date(), level, null, arg0);
		}
	}

	private void execute(Level level, String arg0, Object arg1) {
		if(checkEnabled(level)) {
			logLine(new Date(), level, null, arg0, arg1);
		}
	}

	public void execute(Level level, String arg0, Object[] arg1) {
		if(checkEnabled(level)) {
			logLine(new Date(), level, null, arg0, arg1);
		}
	}

	public void execute(Level level, String arg0, Throwable arg1) {
		if(checkEnabled(level)) {
			logLine(new Date(), level, null, arg0, arg1);
		}
	}

	public void execute(Level level, String arg0, Object arg1, Object arg2) {
		if(checkEnabled(level)) {
			logLine(new Date(), level, null, arg0, arg1, arg2);
		}
	}

	public void execute(Level level, Marker arg0, String arg1) {
		if(checkEnabled(level, arg0)) {
			logLine(new Date(), level, arg0, arg1);
		}
	}

	public void execute(Level level, Marker arg0, String arg1, Object arg2) {
		if(checkEnabled(level, arg0)) {
			logLine(new Date(), level, arg0, arg1, arg2);
		}
	}

	public void execute(Level level, Marker arg0, String arg1, Object[] arg2) {
		if(checkEnabled(level, arg0)) {
			logLine(new Date(), level, arg0, arg1, arg2);
		}
	}

	public void execute(Level level, Marker arg0, String arg1, Throwable arg2) {
		if(checkEnabled(level, arg0)) {
			logLine(new Date(), level, arg0, arg1, arg2);
		}
	}

	public void execute(Level level, Marker arg0, String arg1, Object arg2, Object arg3) {
		if(checkEnabled(level, arg0)) {
			logLine(new Date(), level, arg0, arg1, arg2, arg3);
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

	void addMarker(Marker marker, Level level) {
		synchronized(m_enabledMarkers) {
			m_enabledMarkers.put(marker, level);
		}
	}

	void removeMarker(Marker marker) {
		synchronized(m_enabledMarkers) {
			m_enabledMarkers.remove(marker);
		}
	}

	boolean isDisabled() {
		return m_disabled;
	}

	void setDisabled(boolean disabled) {
		m_disabled = disabled;
	}

	private Level getLevel() {
		if(m_level != null) {
			return m_level;
		}
		return m_rootConfig.getLevel();
	}

	void setLevel(Level level) {
		m_level = level;
	}

	void setOut(@Nullable String out) {
		synchronized(m_writeLock) {
			m_out = out;
		}
	}

}
