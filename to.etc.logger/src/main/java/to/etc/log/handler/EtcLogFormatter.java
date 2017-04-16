package to.etc.log.handler;

import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import to.etc.log.event.EtcLogEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * Formats logger event.
 * Does logging according to provided configurable {@link EtcLogFormat} formatting pattern.
 * It also does detailed logging of {@link Throwable} log argument if such is logged.
 * It logs up to 5 nested cause levels.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class EtcLogFormatter {
	@Nonnull
	private static final ThreadLocal<SimpleDateFormat>	TIMEFORMATTER	= new ThreadLocal<SimpleDateFormat>() {
																			@Override
																			protected SimpleDateFormat initialValue() {
																				return new SimpleDateFormat(EtcLogFormat.TIMESTAMP);
																			}
																		};

	@Nonnull
	static String format(@Nonnull EtcLogEvent event, @Nonnull String format, @Nullable String filterData) {
		StringBuilder sb = new StringBuilder();

		//-- Get all % markers and replace it with log values.
		int ix = 0;
		int len = format.length();

		while(ix < len) {
			char c = format.charAt(ix++);
			if(c == '%') {
				//detecting if we are in some of format pattern markers
				if(ix + 1 < len) {
					char nextChar = format.charAt(ix++);
					boolean notReplaced = false;
					switch(nextChar){
						case 'd':
						case 'D': //%d (timestamp)
							sb.append(TIMEFORMATTER.get().format(event.getTimestamp()));
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
							if((len >= ix + 2) && "msg".equalsIgnoreCase(format.substring(ix - 1, ix + 2))) {
								ix += 2;
								handleMsg(event, sb);
							} else if((len >= ix + 2) && "mdc".equalsIgnoreCase(format.substring(ix - 1, ix + 2))) {
								ix += 2;
								ix = handleMdc(format, ix, sb);
							} else if((len >= ix + 5) && "marker".equalsIgnoreCase(format.substring(ix - 1, ix + 5))) {
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
			sb.append("\n").append("---------- THROWN ---------- " + t.getClass()).append("\n");
			logThrowable(sb, 0, t, true);
			int loggedCauses = 0;
			while(t.getCause() != null && t != t.getCause()) {
				t = t.getCause();
				loggedCauses++;
				sb.append("---------- NESTED CAUSE (" + loggedCauses + ") ---------- " + t.getClass()).append("\n");
				logThrowable(sb, loggedCauses, t, true);
			}
		}
		return sb.toString();
	}

	private static void handleMsg(@Nonnull EtcLogEvent event, @Nonnull StringBuilder sb) {
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

	private static int handleMdc(@Nonnull String format, int ix, @Nonnull StringBuilder sb) {
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

	private static void logThrowable(@Nonnull StringBuilder sb, int causeIndex, @Nonnull Throwable t, boolean checkNextExceptions) {
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
				sb.append("---------- NEXT EXCEPTION (" + loggedNextExceptions + ") ---------- " + t.getClass()).append("\n");
				logThrowable(sb, loggedNextExceptions, sx, false);
			}
		}
	}
}
