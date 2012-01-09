package to.etc.log;

import java.text.*;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class NonStupidLogFormatter extends Formatter {
	static private final DateFormat	m_df	= new SimpleDateFormat("HH:mm:ss");

	@Override
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();
		sb.append(m_df.format(new Date(record.getMillis())));
		sb.append(' ');
		sb.append(record.getMessage());
		sb.append(" (");
		sb.append(record.getLoggerName());
		String m = record.getSourceMethodName();
		if(m != null && m.length() > 0) {
			sb.append(" @");
			sb.append(m);
		}
		sb.append(")\n");
		return sb.toString();
	}
}
