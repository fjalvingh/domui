package to.etc.domui.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public interface ContextMaker {
	public boolean	handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
