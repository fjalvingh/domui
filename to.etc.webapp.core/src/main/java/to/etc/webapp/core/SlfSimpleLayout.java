/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.webapp.core;

//import java.util.*;

//import ch.qos.logback.classic.spi.*;
//import ch.qos.logback.core.*;

//import to.etc.util.*;

public class SlfSimpleLayout {
/*
 * Code is temporary commented out - we can't reference logback implementation currently 
 * 
 * extends LayoutBase<ILoggingEvent> {
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
*/
}
