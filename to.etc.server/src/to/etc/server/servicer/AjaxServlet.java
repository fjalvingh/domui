package to.etc.server.servicer;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.server.ajax.*;
import to.etc.server.injector.*;
import to.etc.server.servlet.*;
import to.etc.util.*;

public class AjaxServlet extends ContextServletBase {
	static public final String		NOVALUE	= "($$NV##)";

	private ServiceCaller			m_caller;

	private ServiceCaller			m_bulkcaller;

	private IServiceAuthenticator	m_authenticator;

	public AjaxServlet() {
		super(false);
	}

	@Override
	public ContextServletContext makeContext(HttpServletRequest req, HttpServletResponse res, boolean ispost) {
		return new ServiceServerContext(this, req, res, ispost);
	}

	public final ServiceCaller getRequestCaller() {
		return m_caller;
	}

	public final ServiceCaller getBulkCaller() {
		return m_bulkcaller;
	}

	public IServiceAuthenticator getAuthenticator() {
		return m_authenticator;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void init(ServletConfig cf) throws ServletException {
		super.init(cf);
		try {
			File configfile = null;
			String config = cf.getInitParameter("config");
			if(config != null) {
				File f = new File(cf.getServletContext().getRealPath(config));
				if(f.exists())
					configfile = f;
				else
					throw new UnavailableException("The config file '" + config + "' specified in the 'config' init param does not exist as " + f);
			}

			//-- Injector and caller for URL requests
			Injector injector = new Injector();
			injector.initFromServlet(cf); // Generic injector init
			if(configfile != null)
				injector.loadConfig(configfile);
			injector.addRetrieverProvider(new RequestRetrieverProvider());
			injector.addRetrieverProvider(new RequestParameterRetrieverProvider());
			injector.addRetrieverProvider(new BeanRetrieverProvider());
			m_caller = new ServiceCaller(injector);

			//-- Injector and caller for bulk requests. These accept parameters from a JSON parameter map.
			injector = new Injector();
			if(configfile != null)
				injector.loadConfig(configfile);
			injector.initFromServlet(cf); // Generic injector init
			injector.addRetrieverProvider(new RequestRetrieverProvider());
			injector.addRetrieverProvider(new BeanRetrieverProvider());
			injector.addRetrieverProvider(new JSONMapRetrieverProvider());
			m_bulkcaller = new ServiceCaller(injector);

			m_bulkcaller.addSourceClass(ServiceServerContext.class);
			//			m_caller.addSourceClass(Map.class);
			String param = cf.getInitParameter("default-package-list");
			if(param != null) {
				m_caller.setDefaultPackageList(param);
				m_bulkcaller.setDefaultPackageList(param);
			}

			param = cf.getInitParameter("injector-source-classes");
			if(param != null) {
				m_caller.setInjectorSourceClasses(param);
				m_bulkcaller.setInjectorSourceClasses(param);
			}

			param = cf.getInitParameter("default-format");
			if(param != null) {
				m_caller.setDefaultResponseFormat(ResponseFormat.valueOf(param));
			}
			m_bulkcaller.setDefaultResponseFormat(ResponseFormat.JSON);

			param = cf.getInitParameter("login-authenticator");
			if(param != null) {
				Class<IServiceAuthenticator> clz = (Class<IServiceAuthenticator>) ClassUtil.loadClass(getClass().getClassLoader(), param);
				m_authenticator = clz.newInstance();
			} else {
				m_authenticator = new IServiceAuthenticator() {
					public boolean userHasRole(String roleName) throws Exception {
						ServiceServerContext ctx = ServiceServerContext.getCurrent();
						return ctx.getRequest().isUserInRole(roleName);
					}
				};
			}
		} catch(ServletException x) { // Fucking nonsense checked exception shit. James Gosling is an idiot.
			throw x;
		} catch(RuntimeException x) {
			throw x;
		} catch(Error x) {
			throw x;
		} catch(Exception x) {
			throw new WrappedException(x);
		}
	}
}
