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
package to.etc.domui.parts;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;

/**
 * Base class for Parts that refer back to a page component. This handles the
 * encoding and decoding of the part to URL, and decodes all that is needed to
 * find the Page, Conversation and NodeBase of the component in question.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public class ComponentPartRenderer {
	private String[] m_args;

	private Class< ? extends UrlPage> m_pageClass;

	private Page m_page;

	private NodeBase m_component;

	public void initialize(DomApplication app, RequestContextImpl param, String rurl) throws Exception {
		//-- Bugfix: Tomcat 7 does not properly remove ;jsessionid from the URL. So let's do it here. It's wrong ofc because we're not supposed to know that is the way sessions are passed.
		int jsid = rurl.toLowerCase().indexOf(";jsessionid=");
		if(jsid != -1) {
			rurl = rurl.substring(0, jsid);									// Remove ;jsessionid and all after.
		}

		//-- Unstring the pathname, in the format: cid/class/componentid/type
		m_args = rurl.split("/");
		if(m_args.length < 3)
			throw new IllegalStateException("Invalid input URL '" + rurl + "': must be in format cid/pageclass/componentID/resourceType.");
		String cids = m_args[0];
		String pname = m_args[1];
		String wid = m_args[2];

		//-- 1. Get required parameters and retrieve the proper Page
		if(pname.length() == 0)
			throw new IllegalStateException("Missing 'c' parameter (page class name)");
		if(cids.length() == 0)
			throw new IllegalStateException("Missing 'cid' parameter");
		m_pageClass = app.loadPageClass(pname);
		m_page = PageMaker.findPageInConversation(param, m_pageClass, cids);
		if(m_page == null)
			throw new ThingyNotFoundException("The page " + pname + " cannot be found in conversation " + cids);

		//-- Locate the component
		if(wid == null)
			throw new IllegalStateException("Missing 'id' parameter");
		m_component = m_page.findNodeByID(wid);
		if(m_component == null)
			throw new ThingyNotFoundException("The component " + wid + " on page " + pname + " cannot be found in conversation " + cids);

		CidPair cida = CidPair.decode(cids);
		WindowSession windowSession = param.getSession().findWindowSession(cida.getWindowId());
		param.internalSetWindowSession(windowSession);
	}

	public String[] getArgs() {
		return m_args;
	}

	public Class< ? extends UrlPage> getPageClass() {
		return m_pageClass;
	}

	public Page getPage() {
		return m_page;
	}

	public NodeBase getComponent() {
		return m_component;
	}

	public ConversationContext getConversation() {
		return m_page.getConversation();
	}

	/**
	 * Create a ComponentPartFactory reference URL
	 * @param sb
	 * @param b
	 */
	static public void appendComponentURL(StringBuilder sb, Class< ? extends IPartFactory> fclazz, NodeBase b, IRequestContext ctx) {
		sb.append(ctx.getRelativePath(fclazz.getName())); // Root containing the factory name,
		sb.append(".part/");
		sb.append(b.getPage().getConversation().getFullId());
		sb.append("/");
		sb.append(b.getPage().getBody().getClass().getName());
		sb.append("/");
		sb.append(b.getActualID());


		if(ctx instanceof RequestContextImpl) {
			IServerSession hs = ctx.getServerSession(true);
			if(null == hs)
				throw new IllegalStateException("?");
			String sessid = hs.getId();
			sb.append(";jsessionid=").append(sessid);
		}
	}
}
