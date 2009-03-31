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
	private String[]		m_args;
	private Class<? extends UrlPage>	m_pageClass;
	private Page			m_page;
	private NodeBase		m_component;

	public void	initialize(DomApplication app, RequestContextImpl param, String rurl) throws Exception {
		//-- Unstring the pathname, in the format: cid/class/componentid/type
		m_args = rurl.split("/");
		if(m_args.length < 3)
			throw new IllegalStateException("Invalid input URL '"+rurl+"': must be in format cid/pageclass/componentID/resourceType.");
		String	cids	= m_args[0];
		String	pname	= m_args[1];
		String	wid		= m_args[2];

		//-- 1. Get required parameters and retrieve the proper Page
		if(pname.length() == 0)
			throw new IllegalStateException("Missing 'c' parameter (page class name)");
		if(cids.length() == 0)
			throw new IllegalStateException("Missing 'cid' parameter");
		m_pageClass	= app.loadPageClass(pname);
		m_page	= PageMaker.findPageInConversation(param, m_pageClass, cids);
		if(m_page == null)
			throw new ThingyNotFoundException("The page "+pname+" cannot be found in conversation "+cids);

		//-- Locate the component
		if(wid == null)
			throw new IllegalStateException("Missing 'id' parameter");
		m_component	= m_page.findNodeByID(wid);
		if(m_component == null)
			throw new ThingyNotFoundException("The component "+wid+" on page "+pname+" cannot be found in conversation "+cids);
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
	static public void	appendComponentURL(StringBuilder sb, Class<? extends PartFactory> fclazz, NodeBase b, RequestContext ctx) {
		sb.append(ctx.getRelativePath(fclazz.getName()));			// Root containing the factory name,
		sb.append(".part/");
		sb.append(b.getPage().getConversation().getFullId());
		sb.append("/");
		sb.append(b.getPage().getBody().getClass().getName());
		sb.append("/");
		sb.append(b.getActualID());
	}
}
