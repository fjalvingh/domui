package to.etc.domui.parts;

import java.io.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.xml.*;

/**
 * This will become a simple, generic framework to pass events to a
 * browser based application by means of polling: the browser is supposed
 * to poll this part every 2 minutes; this part can pass back event
 * commands to the browser when needed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2010
 */
public class PollInfo implements IUnbufferedPartFactory {
	public void generate(DomApplication app, String rurl, RequestContextImpl param) throws Exception {
		param.getResponse().setContentType("text/xml; charset=UTF-8");
		Writer w = param.getResponse().getWriter();
		XmlWriter	xw	= new XmlWriter(w);
		xw.wraw("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		xw.tag("poll-info");


		xw.tagendnl();
	}
}
