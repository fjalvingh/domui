package to.etc.log.handler;

import java.text.*;

import javax.annotation.*;

import to.etc.log.event.*;

/**
 * Formats logger event.
 * Current basic implementation does not enable configurable formatting.
 * However, it does detailed logging of {@link Throwable} log argument if such is logged.
 * It logs up to 5 nested cause levels.
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class EtcLogFormatter {
	private static final DateFormat	m_tf	= new SimpleDateFormat("HH:mm:ss.SSS");

	static String format(@Nonnull EtcLogEvent event, @Nullable String filterData) {
		StringBuilder sb = new StringBuilder();
		synchronized(m_tf) {
			sb.append(m_tf.format(event.getTimestamp())).append("\t");
		}
		sb.append(event.getLevel().name()).append("\t");
		if(filterData != null) {
			sb.append(filterData).append("\t");
		}
		sb.append(event.getThread().getName()).append("\t");
		if(event.getMarker() != null) {
			sb.append("#").append(event.getMarker().getName()).append("#").append("\t");
		}
		sb.append("[").append(event.getLogger().getName()).append("]").append("\t");
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
		if(event.getThrown() != null) {
			Throwable t = event.getThrown();
			sb.append("\n").append("---------- THROWN ---------- " + t.getClass()).append("\n");
			logThrowable(sb, 0, t);
			int loggedCauses = 0;
			while(t.getCause() != null && t != t.getCause() && loggedCauses++ < 5) {
				t = t.getCause();
				sb.append("---------- NESTED CAUSE (" + loggedCauses + ") ---------- " + t.getClass()).append("\n");
				logThrowable(sb, loggedCauses, t);
			}
			if(loggedCauses >= 5 && t.getCause() != null && t != t.getCause()) {
				sb.append("---------- TRUNCATED OTHER CAUSES ---------- ").append("\n");
			}
		}
		return sb.toString();
	}

	private static void logThrowable(StringBuilder sb, int nestedLevel, Throwable t) {
		if(t.getMessage() != null) {
			sb.append("- message: ").append(t.getMessage()).append("\n");
		}
		StackTraceElement[] stacktrace = t.getStackTrace();
		for(StackTraceElement stack : stacktrace) {
			sb.append("- at ").append(stack.getClassName()).append(" (").append(stack.getMethodName()).append(":").append(stack.getLineNumber()).append(")\n");
		}
	}
}
