package to.etc.log.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import to.etc.log.event.EtcLogEvent;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * Combines following markers with free text in format definition:
 *
 * %d (timestamp, format can be set in SimpleDateFormat format using the timeformat attribute)
 * %l (logger name)
 * %msg (logged message)
 * %n (new line)
 * %p (level)
 * %t (thread)
 * %mdc{key} (mdc value for key, if it is missing skips log part)
 * %marker (outputs marker name, if such is provided in logged line)
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Nov 5, 2012
 */
final public class EtcLogFormat {
	private static final String DEFAULTFORMAT = "%d %p %mdc{loginId}\t%t [%l] %msg";

	private static final String DEFAULTTIME = "HH:mm:ss";

	public static final EtcLogFormat DEFAULT = new EtcLogFormat(DEFAULTFORMAT, DEFAULTTIME);

	private final String m_timeFormat;

	@NonNull
	private final String m_format;

	public EtcLogFormat(@NonNull String format, @NonNull String timeFormat) {
		m_format = format;
		m_timeFormat = timeFormat;
	}

	@NonNull
	static EtcLogFormat createFromXml(@NonNull Node node) {
		Node pa = node.getAttributes().getNamedItem("pattern");
		String pattern = pa == null ? DEFAULTFORMAT : pa.getNodeValue();
		pa = node.getAttributes().getNamedItem("timeformat");
		String time = pa == null ? DEFAULTTIME : pa.getNodeValue();
		return new EtcLogFormat(pattern, time);
	}

	void saveToXml(@NonNull Document doc, @NonNull Element formatNode) {
		formatNode.setAttribute("pattern", m_format);
		formatNode.setAttribute("timeformat", m_timeFormat);
	}

	@NonNull
	private final ThreadLocal<SimpleDateFormat> m_timeFormatter = new ThreadLocal<>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(m_timeFormat);
		}
	};

	@NonNull
	String format(@NonNull EtcLogEvent event, @Nullable String filterData) {
		StringBuilder sb = new StringBuilder();

		//-- Get all % markers and replace it with log values.
		int ix = 0;
		int len = m_format.length();

		while(ix < len) {
			char c = m_format.charAt(ix++);
			if(c == '%') {
				//detecting if we are in some of format pattern markers
				if(ix + 1 < len) {
					char nextChar = m_format.charAt(ix++);
					boolean notReplaced = false;
					switch(nextChar) {
						case 'd':
						case 'D': //%d (timestamp)
							sb.append(m_timeFormatter.get().format(event.getTimestamp()));
							break;
						case 'l':
						case 'L': //%l (logger name)
							sb.append(event.getLogger().getName());
							break;
						case 'n':
						case 'N': //%n (new line)
							sb.append("\n");
							break;
						case 'p':
						case 'P': //%p (level)
							sb.append(event.getLevel());
							break;
						case 't':
						case 'T': //%t (thread)
							sb.append(event.getThread().getName());
							break;
						case 'm':
						case 'M': //multiple choices
							if((len >= ix + 2) && "msg".equalsIgnoreCase(m_format.substring(ix - 1, ix + 2))) {
								ix += 2;
								handleMsg(event, sb);
							} else if((len >= ix + 2) && "mdc".equalsIgnoreCase(m_format.substring(ix - 1, ix + 2))) {
								ix += 2;
								ix = handleMdc(m_format, ix, sb);
							} else if((len >= ix + 5) && "marker".equalsIgnoreCase(m_format.substring(ix - 1, ix + 5))) {
								ix += 5;
								Marker marker = event.getMarker();
								sb.append(marker != null ? marker.getName() : "");
							} else {
								sb.append("%").append(nextChar);
							}
							break;
						default:
							sb.append("%").append(nextChar);
					}
				}
			} else {
				sb.append(c);
			}
		}

		Throwable t = event.getThrown();
		if(t != null) {
			sb.append("\n").append("---------- THROWN ---------- ").append(t.getClass()).append("\n");
			logThrowable(sb, 0, t, true);
			int loggedCauses = 0;
			while(t.getCause() != null && t != t.getCause()) {
				t = t.getCause();
				loggedCauses++;
				sb.append("---------- NESTED CAUSE (").append(loggedCauses).append(") ---------- ").append(t.getClass()).append("\n");
				logThrowable(sb, loggedCauses, t, true);
			}
		}
		return sb.toString();
	}

	private static void handleMsg(@NonNull EtcLogEvent event, @NonNull StringBuilder sb) {
		Object[] args = event.getArgs();
		if(args != null && args.length > 0) {
			FormattingTuple tuple;
			if(args.length == 1) {
				tuple = MessageFormatter.format(event.getMsg(), args);
			} else if(args.length == 2) {
				tuple = org.slf4j.helpers.MessageFormatter.format(event.getMsg(), args[0], args[1]);
			} else {
				tuple = org.slf4j.helpers.MessageFormatter.arrayFormat(event.getMsg(), args);
			}
			sb.append(tuple.getMessage());
		} else {
			sb.append(event.getMsg());
		}
	}

	private static int handleMdc(@NonNull String format, int ix, @NonNull StringBuilder sb) {
		if(format.charAt(ix) != '{') {
			return ix;
		} else {
			int pos = format.indexOf("}", ix);
			if(pos == -1) {
				return ix;
			} else {
				String key = format.substring(ix + 1, pos).trim();
				String value = MDC.get(key);
				if(value != null) {
					sb.append(value);
				}
				return pos + 1;
			}
		}
	}

	private static void logThrowable(@NonNull StringBuilder sb, int causeIndex, @NonNull Throwable t, boolean checkNextExceptions) {
		if(t.getMessage() != null) {
			sb.append("- message: ").append(t.getMessage()).append("\n");
		}
		StackTraceElement[] stacktrace = t.getStackTrace();
		for(StackTraceElement stack : stacktrace) {
			sb.append("   ").append(stack.toString()).append("\n");
		}
		if(!checkNextExceptions) {
			return;
		}
		if(t instanceof SQLException) {
			SQLException sx = (SQLException) t;
			int loggedNextExceptions = 0;
			while(sx.getNextException() != null && sx != sx.getNextException()) {
				sx = sx.getNextException();
				loggedNextExceptions++;
				sb.append("---------- NEXT EXCEPTION (").append(loggedNextExceptions).append(") ---------- ").append(t.getClass()).append("\n");
				logThrowable(sb, loggedNextExceptions, sx, false);
			}
		}
	}

	@NonNull
	public String getFormat() {
		return m_format;
	}

	public String getTimeFormat() {
		return m_timeFormat;
	}
}
