package to.etc.server.injector;

import java.lang.annotation.*;

import javax.servlet.http.*;

import org.w3c.dom.*;

import to.etc.server.servlet.*;
import to.etc.xml.*;

/**
 * A retriever which retrieves a given 'target' class from a servlet's HttpSession. If the
 * object does not exist in the class it gets created.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 15, 2007
 */
public class SessionObjectRetriever implements RetrieverProvider, XmlParameterizable {
	protected Class		m_targetClass;

	protected boolean	m_create;

	protected String	m_sessionKey;

	protected String	m_name;

	/**
	 * Configure this retriever.
	 * @see to.etc.server.injector.XmlParameterizable#configure(org.w3c.dom.Node)
	 */
	public void configure(Node data) throws Exception {
		String tgt = DomTools.strAttr(data, "target-class");
		if(tgt == null)
			throw new IllegalStateException(getClass().getName() + " requires a 'target-class' parameter.");
		Class cl;
		try {
			cl = Class.forName(tgt);
		} catch(Exception x) {
			throw new IllegalStateException(getClass().getName() + ": target-class " + tgt + " cannot be loaded: " + x);
		}
		m_targetClass = cl;

		//-- Must this be created?
		m_create = DomTools.boolAttr(data, "create", true); // Create by default
		m_sessionKey = DomTools.strAttr(data, "sessionKey", tgt); // Get name; use classname by default.
		m_name = DomTools.strAttr(data, "name", null);
		//		System.out.println("injector: configured session retriever for "+m_targetClass+" with name "+m_name+", sessionkey="+m_sessionKey);
	}

	public Retriever makeRetriever(Class sourcecl, Class targetcl, String name, Annotation[] pann) {
		//		System.out.println("injector: check SessionObjectRv source="+sourcecl+", target="+targetcl+", name="+name);
		if(!targetcl.isAssignableFrom(m_targetClass)) // Cannot be assigned to
			return null;
		if(m_name != null && !m_name.equals(name)) // Name required but not matched?
			return null;

		//-- We can assign. Accept the basic session holders as source class.
		if(RequestContext.class.isAssignableFrom(sourcecl) || HttpServletRequest.class.isAssignableFrom(sourcecl)) {
			return new Retriever() {
				public void releaseObject(Object o) {
				}

				public Object retrieveValue(Object source) throws Exception {
					HttpSession ses;
					if(source == null)
						return null;
					if(source instanceof RequestContext)
						ses = ((RequestContext) source).getRequest().getSession(true);
					else if(source instanceof HttpServletRequest)
						ses = ((HttpServletRequest) source).getSession(true);
					else
						throw new IllegalStateException("Unexpected source in retriever: " + source);
					Object val = ses.getAttribute(m_sessionKey); // Is the thingy present in the session?
					if(val == null) {
						if(m_create) {
							val = m_targetClass.newInstance();
							ses.setAttribute(m_sessionKey, val);
						}
					}
					return val;
				}

				public Class getType() {
					return m_targetClass;
				}

				public String getDisplayName() {
					return "session." + m_sessionKey;
				}
			};
		}
		return null;
	}
}
