package to.etc.log.idioticjavasystemloggarbage;

import to.etc.log.EtcLogger;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * The idiots making the JDK decided to implement Yet Another Logging Framework, which
 * as usual causes problems instead of fixing them. This wraps the System.Logger garbage
 * and routes it through the EtcLogger for as far at that goes.
 *
 * You do not need to be an idiot to become a Java architect but clearly it DOES help.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
final public class GarbageLogger implements System.Logger {
	private final EtcLogger m_logger;

	public GarbageLogger(EtcLogger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "System.LoggerIdioticLogger";
	}

	@Override
	public boolean isLoggable(Level level) {
		switch(level) {
			default:
				return false;

			case ALL:
			case TRACE:
				return m_logger.isTraceEnabled();

			case DEBUG:
				return m_logger.isDebugEnabled();

			case ERROR:
				return m_logger.isErrorEnabled();

			case WARNING:
				return m_logger.isWarnEnabled();

			case INFO:
				return m_logger.isInfoEnabled();

			case OFF:
				return false;
		}
	}

	@Override
	public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
		switch(level) {
			default:
				return;

			case ALL:
			case INFO:
				m_logger.info(msg, thrown);
				return;

			case TRACE:
				m_logger.trace(msg, thrown);
				return;

			case DEBUG:
				m_logger.debug(msg, thrown);
				return;

			case ERROR:
				m_logger.error(msg, thrown);
				return;

			case WARNING:
				m_logger.warn(msg, thrown);
				return;

			case OFF:
				return;
		}
	}

	@Override
	public void log(Level level, ResourceBundle bundle, String format, Object... params) {
		/*
		 * The SSL logging trainwreck passes parameters that contain the data dump but the format
		 * does not contain a placeholder for it. So: if we see params but after MessageFormat
		 * we see that the output is the same as the input then we add the params.
		 */
		String text = MessageFormat.format(format, params);
		if(params != null && params.length > 0) {
			if(format.equals(text)) {
				//-- Jah, probably bad.
				StringBuilder sb = new StringBuilder();
				sb.append(format);
				for(Object param : params) {
					if(param != null) {
						sb.append(String.valueOf(param));
						if(sb.charAt(sb.length() - 1) != '\n')
							sb.append("\n");
					}
				}
				text = sb.toString();
			}
		}

		log(level, bundle, text, (Throwable) null);
	}
}
