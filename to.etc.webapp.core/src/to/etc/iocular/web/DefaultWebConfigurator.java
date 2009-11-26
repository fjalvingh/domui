package to.etc.iocular.web;

import javax.servlet.*;

import to.etc.iocular.*;
import to.etc.iocular.def.*;
import to.etc.iocular.util.*;

/**
 * Default implementation of a web configurator. This version gets used if no
 * specific implementation is defined in the web configuration. The default
 * configurator uses context properties to load the container definitions for
 * session, request and application.
 *
 * @author jal
 * Created on Mar 25, 2007
 */
public class DefaultWebConfigurator implements WebConfigurator {
	public WebConfiguration createConfiguration(ServletContext ctx) throws Exception {
		//-- 1. Try bulk
		ContainerDefinition reqd, sesd, appd;
		String cn = ctx.getInitParameter("request-configurator-class");
		if(cn != null) {
			Configurator c = ClassUtil.instanceByName(Configurator.class, cn);
			reqd = c.getContainerDefinition();
		} else {
			throw new UnavailableException("Missing 'request-configurator-class' context-parameter");
		}

		cn = ctx.getInitParameter("session-configurator-class");
		if(cn != null) {
			Configurator c = ClassUtil.instanceByName(Configurator.class, cn);
			sesd = c.getContainerDefinition();
		} else {
			throw new UnavailableException("Missing 'session-configurator-class' context-parameter");
		}

		cn = ctx.getInitParameter("application-configurator-class");
		if(cn != null) {
			Configurator c = ClassUtil.instanceByName(Configurator.class, cn);
			appd = c.getContainerDefinition();
		} else {
			throw new UnavailableException("Missing 'application-configurator-class' context-parameter");
		}
		return new WebConfiguration(appd, sesd, reqd);
	}
}
