package to.etc.webapp.ajax.comet;

import javax.servlet.http.*;

/**
 * A context call for a COMET environment.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 25, 2006
 */
public interface CometContext {
	public void begin(HttpServlet slet, HttpServletRequest req, Continuation cont) throws Exception;

	public void respond(HttpServletResponse resp, boolean timeout) throws Exception;
}
