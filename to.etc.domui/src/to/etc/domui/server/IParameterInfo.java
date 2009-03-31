package to.etc.domui.server;

/**
 * Abstract thingy to get parameters for a page/request.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 4, 2008
 */
public interface IParameterInfo {
	public String	getParameter(String name);
	public String[]	getParameters(String name);
	public String[]	getParameterNames();
}
