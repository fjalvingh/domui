package to.etc.webapp.core;

import java.util.*;

import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.*;

import to.etc.util.*;

public class SlfSimpleLayout extends LayoutBase<ILoggingEvent> {
	@Override
	public String doLayout(ILoggingEvent e) {
		StringBuilder sb = new StringBuilder(512);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(e.getTimeStamp());
		sb.append(StringTool.intToStr(cal.get(Calendar.HOUR_OF_DAY), 10, 2));
		sb.append(StringTool.intToStr(cal.get(Calendar.MINUTE), 10, 2));
		sb.append(StringTool.intToStr(cal.get(Calendar.SECOND), 10, 2));
		sb.append('.');
		sb.append(StringTool.intToStr(cal.get(Calendar.MILLISECOND), 10, 3));

		sb.append(' ');
		sb.append(e.getFormattedMessage());
		sb.append(" (");
		sb.append(e.getCallerData()[0].getClassName());
		sb.append('.');
		sb.append(e.getCallerData()[0].getMethodName());
		sb.append(")");
		sb.append('\n');
		if(e.getThrowableProxy() != null) {
			sb.append(ThrowableProxyUtil.asString(e.getThrowableProxy()));
		}
		return sb.toString();
	}
}
