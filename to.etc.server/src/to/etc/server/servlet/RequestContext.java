package to.etc.server.servlet;

import javax.servlet.http.*;

/**
 * An informational base class for a request context. This generically encapsulates 
 * all that should be known about a request and it's context.
 *
 * @author jal
 * Created on Jan 19, 2006
 */
public interface RequestContext {
	/**
	 * The request thing to use. Tnis MAY be a wrapped request for instance
	 * when file encoding is used on the input stream.
	 * @return
	 */
	public HttpServletRequest getRequest();

	/**
	 * The response thingy.
	 * @return
	 */
	public HttpServletResponse getResponse();

	/**
	 * The servlet handling this request.
	 * @return
	 */
	public HttpServlet getServlet();

	/**
	 * Returns T if the request was a post.
	 * @return
	 */
	public boolean isPost();

	public WebsiteInfo getSiteInfo();
}
