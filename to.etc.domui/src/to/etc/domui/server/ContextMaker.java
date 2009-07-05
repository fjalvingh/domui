package to.etc.domui.server;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * How to handle a filter request.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public interface ContextMaker {
	public boolean handleRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws Exception;
}
