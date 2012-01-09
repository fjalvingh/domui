package to.etc.log;

import java.text.*;
import java.util.*;

/**
 * Writes log events to the screen.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class ScreenEventWriterThing implements iLogEventListener {
	/// The filter
	private LinkedList	m_filter;

	public ScreenEventWriterThing(LinkedList filter) {
		m_filter = filter;

		//		for(Iterator i = m_filter.iterator(); i.hasNext();)
		//		{
		//			String s	= (String) i.next();
		//			System.out.println("Interest: "+s);
		//		}
	}


	public void logEvent(LogRecord lr) throws Exception {
		//-- 1. Get time only,
		DateFormat df = DateFormat.getTimeInstance();
		System.out.print(df.format(lr.m_ts));

		//		System.out.print(lr.m_ts.toString());
		System.out.print(" ");
		if(lr.m_cat != null) {
			System.out.print("{");
			System.out.print(lr.m_cat.getName());
			System.out.print("} ");
		}
		System.out.println(lr.m_msg);

		//-- Now the stack dump && the like
		if(lr.m_x != null) {
			System.out.println("Exception: " + lr.m_x.toString());
			lr.m_x.printStackTrace();
		}
	}

	public boolean isInterestedIn(Category c) {

		boolean isi = LogMaster.checkInterestedIn(m_filter, c.getName());
		//		System.out.println("Reg: is interested in "+c.getName()+" is "+isi);
		return isi;
	}
}
