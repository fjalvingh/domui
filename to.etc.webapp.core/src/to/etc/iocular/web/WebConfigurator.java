package to.etc.iocular.web;

import javax.servlet.ServletContext;

/**
 * Default implementation of a thingy which has to configure the
 * web application when started.
 *
 * @author jal
 * Created on Mar 25, 2007
 */
public interface WebConfigurator {
	public WebConfiguration	createConfiguration(ServletContext ctx) throws Exception;
}
