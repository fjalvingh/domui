package to.etc.domui.parts;

import java.io.*;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;


/**
 * Polled to keepalive.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2010
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "OS_OPEN_STREAM", justification = "Stream is closed by servlet code")
public class PollInfo implements IUnbufferedPartFactory {
	@Override
	public void generate(@Nonnull DomApplication app, @Nonnull String rurl, @Nonnull RequestContextImpl param) throws Exception {
		param.getResponse().setContentType("text/html; charset=UTF-8");
		Writer w = param.getResponse().getWriter();
		w.write("<html><body>");

		w.write("</body></html>");
	}
}
