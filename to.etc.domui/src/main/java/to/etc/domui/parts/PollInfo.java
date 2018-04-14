package to.etc.domui.parts;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.server.parts.IUnbufferedPartFactory;

import java.io.Writer;


/**
 * Polled to keepalive.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2010
 */
public class PollInfo implements IUnbufferedPartFactory {
	@Override
	public void generate(@NonNull DomApplication app, @NonNull String rurl, @NonNull RequestContextImpl param) throws Exception {
		Writer w = param.getRequestResponse().getOutputWriter("text/html; charset=UTF-8", "utf-8");
		w.write("<html><body>");

		w.write("</body></html>");
	}
}
