package to.etc.log.handler;

import java.text.*;

import javax.annotation.*;

import to.etc.log.event.*;

/**
 * Formats logger event.
 * Current basic implementation does not enable configurable formatting.
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class LogFormatter {
	private static final DateFormat	m_tf	= new SimpleDateFormat("HH:mm:ss.SSS");

	static String format(@Nonnull LogEvent event, @Nullable String filterData) {
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
		sb.append(String.format(event.getMsg(), event.getArgs()));
		return sb.toString();
	}
}
