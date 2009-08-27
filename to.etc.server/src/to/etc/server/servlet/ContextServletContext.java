package to.etc.server.servlet;

/**
 * Created on Feb 4, 2005
 * @author jal
 */
public interface ContextServletContext extends RequestContext {
	public void execute() throws Exception;

	public void initialize();

	public void discard();

	public ContextServletBase getServlet();
}
