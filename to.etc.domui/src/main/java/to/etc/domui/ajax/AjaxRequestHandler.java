/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.ajax;

import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IFilterRequestHandler;
import to.etc.domui.server.IRequestInterceptor;
import to.etc.domui.server.RequestContextImpl;
import to.etc.iocular.Container;
import to.etc.iocular.def.ContainerDefinition;
import to.etc.iocular.ioccontainer.BasicContainer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * This handles .ajax requests.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 14, 2009
 */
public class AjaxRequestHandler implements IFilterRequestHandler {
	static private final String CONT_KEY = "ajax.ioc";

	private final DomApplication m_application;

	//	private IInstanceBuilder m_instanceBuilder;

	private List<IRequestInterceptor> m_interceptorList = new ArrayList<IRequestInterceptor>();

	private Container m_applicationContainer;

	private ContainerDefinition m_sessionContainerDef;

	private ContainerDefinition m_requestContainerDef;

	private final RpcCallHandler m_callHandler;

	public AjaxRequestHandler(final DomApplication domApplication) {
		m_application = domApplication;
		m_callHandler = new RpcCallHandler();
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

	//	public IInstanceBuilder getInstanceBuilder() {
	//		return m_instanceBuilder;
	//	}

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
	private Container getRequestContainer(final RequestContextImpl ci) {
		//-- If we have a request thing get it,
		Object v = ci.getAttribute(CONT_KEY);
		if(v != null)
			return (Container) v;

		Container dad;
		if(getSessionContainerDef() == null)
			dad = getApplicationContainer();
		else {
			//-- Obtain/create the session container
			dad = (Container) ci.getSession().getAttribute(CONT_KEY);
			if(dad == null) {
				//-- Create the session container
				dad = new BasicContainer(getSessionContainerDef(), getApplicationContainer());
				ci.getSession().setAttribute(CONT_KEY, dad);
				dad.start();
				//-- FIXME Needs destruction listener.
			}
		}

		//-- Make a basic container, then store
		BasicContainer rq = new BasicContainer(getRequestContainerDef(), dad);
		ci.setAttribute("arq.bc", rq);
		rq.start();
		return rq;
	}

	<T> T makeCallClass(final Class<T> clz, final AjaxRequestContext ctx) throws Exception {
		Container bc = getRequestContainer(ctx.getRctx());
		bc.setParameter(ctx.getRctx());
		return bc.getObject(clz);
	}

	/**
	 * If a request container exists destroy it.
	 * @param ctx
	 */
	private void requestCompleted(final RequestContextImpl ctx) {
		Container co = (Container) ctx.getAttribute(CONT_KEY);
		if(co == null)
			return;
		ctx.setAttribute(CONT_KEY, null);
		co.destroy();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	FilterRequestHandler implementation.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Actual execution by delegating to the context.
	 * @see to.etc.domui.server.IFilterRequestHandler#handleRequest(to.etc.domui.server.RequestContextImpl)
	 */
	@Override
	public boolean handleRequest(final @Nonnull RequestContextImpl ctx) throws Exception {
		if(! ctx.getExtension().equals("xaja"))
			return false;

		AjaxRequestContext ax = new AjaxRequestContext(this, m_callHandler, ctx);
		String rurl = ctx.getInputPath();
		boolean ok = false;
		try {
			ax.execute(rurl);
			ok = true;
			return true;
		} finally {
			try {
				requestCompleted(ctx);
			} catch(Exception x) {
				if(ok)
					throw x;
				x.printStackTrace(); // First exception present; just print the finalizer exception.
			}
		}
	}
}
