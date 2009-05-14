package to.etc.domui.ajax;

import java.util.*;

import to.etc.domui.server.*;
import to.etc.iocular.*;
import to.etc.iocular.def.*;

/**
 * This handles .ajax requests.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 14, 2009
 */
public class AjaxRequestHandler implements FilterRequestHandler {
	private final DomApplication m_application;

	private IInstanceBuilder	m_instanceBuilder;

	private List<IRequestInterceptor> m_interceptorList = new ArrayList<IRequestInterceptor>();

	private Container			m_applicationContainer;

	private ContainerDefinition	m_sessionContainerDef;

	private ContainerDefinition	m_requestContainerDef;

	public AjaxRequestHandler(final DomApplication domApplication) {
		m_application = domApplication;
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

	public void handleRequest(final RequestContextImpl ctx) throws Exception {
		AjaxRequestContext	ax	= new AjaxRequestContext(this, ctx);
		ax.execute();
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
}
