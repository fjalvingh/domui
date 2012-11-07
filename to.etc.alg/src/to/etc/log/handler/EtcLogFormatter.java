package to.etc.log.handler;

import java.sql.*;
import java.text.*;

import javax.annotation.*;

import org.slf4j.*;

import to.etc.log.event.*;

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
	private static final ThreadLocal<SimpleDateFormat>	TIMEFORMATTER	= new ThreadLocal<SimpleDateFormat>() {
																			@Override
																			protected SimpleDateFormat initialValue() {
																				return new SimpleDateFormat(EtcLogFormat.TIMESTAMP);
																			}
																		};

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
								sb.append(event.getMarker() != null ? event.getMarker().getName() : "");
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

		if(event.getThrown() != null) {
			Throwable t = event.getThrown();
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

	private static void handleMsg(EtcLogEvent event, StringBuilder sb) {
		if(event.getArgs() != null && event.getArgs().length > 0) {
			if(event.getArgs().length == 1) {
				sb.append(org.slf4j.helpers.MessageFormatter.format(event.getMsg(), event.getArgs()));
			} else if(event.getArgs().length == 2) {
				sb.append(org.slf4j.helpers.MessageFormatter.format(event.getMsg(), event.getArgs()[0], event.getArgs()[1]));
			} else {
				sb.append(org.slf4j.helpers.MessageFormatter.arrayFormat(event.getMsg(), event.getArgs()));
			}
		} else {
			sb.append(event.getMsg());
		}
	}

	private static int handleMdc(String format, int ix, StringBuilder sb) {
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

	private static void logThrowable(StringBuilder sb, int causeIndex, Throwable t, boolean checkNextExceptions) {
		if(t.getMessage() != null) {
			sb.append("- message: ").append(t.getMessage()).append("\n");
		}
		StackTraceElement[] stacktrace = t.getStackTrace();
		for(StackTraceElement stack : stacktrace) {
			sb.append("   ").append(stack.toString()).append("\n");
		}
		if(t instanceof SQLException) {
			SQLException sx = (SQLException) t;
			int loggedNextExcptions = 0;
			while(sx.getNextException() != null && sx != sx.getNextException()) {
				sx = sx.getNextException();
				loggedNextExcptions++;
				sb.append("---------- NEXT EXCEPTION (" + loggedNextExcptions + ") ---------- " + t.getClass()).append("\n");
				logThrowable(sb, loggedNextExcptions, t, false);
			}
		}
	}
}
