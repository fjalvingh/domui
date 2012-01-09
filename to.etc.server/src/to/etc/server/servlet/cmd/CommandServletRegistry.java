package to.etc.server.servlet.cmd;

import java.lang.reflect.*;
import java.util.*;

/**
 * A container for handlers for the servlet command code.
 *
 * Created on Aug 23, 2005
 * @author jal
 */
public class CommandServletRegistry {
	private Class< ? >	m_defaulthandler_cl;

	/**
	 * The list of registered ManyCmd classes.
	 * Then each class registered is checked for
	 * existence of such a function with the correct parameters. When found the
	 * Method for that class is retrieved so that the function can be called.
	 * The method pointer is saved in a hash table with the function name to
	 * quickly call the function next time since retrieving the method pointer
	 * using reflection is the expensive part.
	 */
	private Hashtable	m_xmlname_ht	= new Hashtable();

	/** The list of registered classes. */
	private ArrayList	m_cmdhandlers_v	= new ArrayList();

	private ArrayList	m_handler_al	= new ArrayList();

	/**
	 * Registers a handler class.
	 *
	 * @param cl the class to register, obtained like <i>Handler.class </i>
	 */
	public void register(Class cl) {
		synchronized(m_cmdhandlers_v) {
			if(m_cmdhandlers_v.contains(cl))
				return; // Already registered

			//-- Create an instance....
			try {
				Object o = cl.newInstance(); // Make a new instance,
				if(o instanceof ICommandServletHandler) // Does this implement the command interface?
				{
					ICommandServletHandler ch = (ICommandServletHandler) o;
					if(ch.getExtentionName() != null) {
						m_handler_al.add(ch);
						return;
					}
				}
				m_cmdhandlers_v.add(cl);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	/**
	 * Can be used to set a default handler: a handler which gets called
	 * if none of the registered handlers wants to handle the request. If
	 * the default handler is present it MUST either report an error
	 * or handle the request.
	 * @param cl
	 */
	public void setDefaultHandler(Class cl) {
		m_defaulthandler_cl = cl;
	}

	/**
	 * Returns all handlers that have a name. These usually are extentions, and
	 * the names are returned in the new-session packet.
	 *
	 * @return
	 */
	public ArrayList getNamedHandlers() {
		synchronized(m_cmdhandlers_v) {
			return m_handler_al;
		}
	}


	/**
	 * Tries to locate a caller method with the appropriate signature in the
	 * class passed.
	 *
	 * @param cl
	 *            the class to check for the method
	 * @param name
	 *            the generated name for the method (execXxxYyy)
	 * @return null, or a method
	 */
	static final private Method findMethod(Class cl, String name) {
		//-- Can I find the appropriate method?
		try {
			return cl.getMethod(name, new Class[]{CommandContext.class});
		} catch(Exception x) {
			return null;
		}
	}


	/**
	 * Tries to find a handler function for the xml command specified. We build
	 * a function name from the xml name by replacing underscores and dashes
	 * with nothing but uppercasing the letter following the dash/ubderscore. It
	 * will also uppercase the first letter of the entire name. The name is then
	 * preceded with 'exec'. So an xml name like <code>get-site-list</code>
	 * will be converted to <code>execGetSiteList</code>. This function must
	 * not be called directly, but is called thru handlerByName, which caches
	 * the functions it has found.
	 *
	 * @param name
	 *            the xml name to convert and lookup.
	 * @return a method, or null if nothing was found
	 */
	private Method findHandlerByName(String name) {
		//-- First create a member name from the XML name,
		StringBuffer sb = new StringBuffer(32);
		sb.append("exec");

		boolean ucnext = true;
		for(int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if(c == '-' || c == '_')
				ucnext = true;
			else {
				if(ucnext)
					sb.append(Character.toUpperCase(c));
				else
					sb.append(Character.toLowerCase(c));
				ucnext = false;
			}
		}
		String fname = sb.toString();

		synchronized(m_cmdhandlers_v) {
			for(Iterator it = m_cmdhandlers_v.iterator(); it.hasNext();) {
				Class cl = (Class) it.next();
				Method m = findMethod(cl, fname);
				if(m != null)
					return m;
			}
		}

		//-- Still nothing.. If there's a default handler class use it's execDefault entry
		if(m_defaulthandler_cl != null) {
			Method m = findMethod(m_defaulthandler_cl, "execDefault");
			if(m != null)
				return m;
		}
		return null;
	}


	/**
	 * Tries to find a handler function for the xml command specified. This
	 * first checks if we already know the method by looking into the xml name
	 * cache. If known it returns the method. If the name is not yet known then
	 * We build a function name from the xml name by replacing underscores and
	 * dashes with nothing but uppercasing the letter following the
	 * dash/ubderscore. It will also uppercase the first letter of the entire
	 * name. The name is then preceded with 'exec'. So an xml name like
	 * <code>get-site-list</code> will be converted to
	 * <code>execGetSiteList</code>. This function must not be called
	 * directly, but is called thru handlerByName, which caches the functions it
	 * has found.
	 *
	 * @param name
	 *            the xml name to convert and lookup.
	 * @return a method, or null if nothing was found
	 */
	public Method handlerByName(String name) {
		String lcname = name.toLowerCase();
		synchronized(m_xmlname_ht) {
			Object o = m_xmlname_ht.get(lcname); // Lookup using lowercase name
			if(o != null)
				return (Method) o; // Return if found;
		}

		//-- Not found. Try the registered classes for one,
		Method m = findHandlerByName(name); // Try all handlers;
		if(m == null)
			return null; // Not found- exit

		//-- Found: register in hashtable. This may occur more than once which
		// is not a problem...
		synchronized(m_xmlname_ht) {
			m_xmlname_ht.put(lcname, m);
		}
		return m;
	}
}
