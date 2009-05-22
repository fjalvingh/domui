package to.etc.domui.ajax;

import java.util.*;
import to.etc.domui.server.*;
import to.etc.iocular.*;
import to.etc.iocular.container.*;
import to.etc.iocular.def.*;

/**
 * This handles .ajax requests.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 14, 2009
 */
public class AjaxRequestHandler implements FilterRequestHandler {
	static private final String		CONT_KEY = "ajax.ioc";

	private final DomApplication m_application;

	private IInstanceBuilder	m_instanceBuilder;

	private List<IRequestInterceptor> m_interceptorList = new ArrayList<IRequestInterceptor>();

	private Container			m_applicationContainer;

	private ContainerDefinition	m_sessionContainerDef;

	private ContainerDefinition	m_requestContainerDef;

	private final RpcCallHandler	m_callHandler;

	public AjaxRequestHandler(final DomApplication domApplication) {
		m_application = domApplication;
		m_callHandler	= new RpcCallHandler();
	}

	public DomApplication getApplication() {
		return m_application;
	}

	public synchronized void addInterceptor(final IRequestInterceptor r) {
		List<IRequestInterceptor> l = new ArrayList<IRequestInterceptor>(m_interceptorList);
		l.add(r);
		m_interceptorList = l;
	}

	public synchronized List<IRequestInterceptor> getInterceptorList() {
		return m_interceptorList;
	}

	public IInstanceBuilder getInstanceBuilder() {
		return m_instanceBuilder;
	}

	public Container getApplicationContainer() {
		return m_applicationContainer;
	}

	public void setApplicationContainer(final Container applicationContainer) {
		m_applicationContainer = applicationContainer;
	}

	public ContainerDefinition getSessionContainerDef() {
		return m_sessionContainerDef;
	}

	public void setSessionContainerDef(final ContainerDefinition sessionContainerDef) {
		m_sessionContainerDef = sessionContainerDef;
	}

	public ContainerDefinition getRequestContainerDef() {
		return m_requestContainerDef;
	}

	public void setRequestContainerDef(final ContainerDefinition requestContainerDef) {
		m_requestContainerDef = requestContainerDef;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handler allocation and injection.					*/
	/*--------------------------------------------------------------*/
	/**
	 * UNSTABLE INTERFACE - must move to separate class (interface).
	 * @return
	 */
	private Container	getRequestContainer(final RequestContextImpl ci) {
		//-- If we have a request thing get it,
		Object v = ci.getAttribute(CONT_KEY);
		if(v != null)
			return (Container) v;

		Container	dad;
		if(getSessionContainerDef() == null)
			dad = getApplicationContainer();
		else {
			//-- Obtain/create the session container
			dad	= (Container)ci.getSession().getAttribute(CONT_KEY);
			if(dad == null) {
				//-- Create the session container
				dad	= new BasicContainer(getSessionContainerDef(), getApplicationContainer());
				ci.getSession().setAttribute(CONT_KEY, dad);
				dad.start();
				//-- FIXME Needs destruction listener.
			}
		}

		//-- Make a basic container, then store
		BasicContainer	rq	= new BasicContainer(getRequestContainerDef(), dad);
		ci.setAttribute("arq.bc", rq);
		rq.start();
		return rq;
	}

	<T>	T	makeCallClass(final Class<T> clz, final AjaxRequestContext ctx) throws Exception {
		Container	bc	= getRequestContainer(ctx.getRctx());
		bc.setParameter(ctx.getRctx());
		return bc.getObject(clz);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	FilterRequestHandler implementation.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Actual execution by delegating to the context.
	 * @see to.etc.domui.server.FilterRequestHandler#handleRequest(to.etc.domui.server.RequestContextImpl)
	 */
	public void handleRequest(final RequestContextImpl ctx) throws Exception {
		AjaxRequestContext	ax	= new AjaxRequestContext(this, m_callHandler, ctx);
		String				rurl= ctx.getInputPath();
		ax.execute(rurl);
	}



}
