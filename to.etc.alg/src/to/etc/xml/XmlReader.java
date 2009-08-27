package to.etc.xml;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Helper thing to allow easy reading of an XML document into
 * a DOM structure.
 * 
 * Created on Mar 30, 2005
 * @author jal
 */
public class XmlReader implements ErrorHandler {
	/*--------------------------------------------------------------*/
	/*	CODING:	Returns a DOM document from the input XML message.	*/
	/*--------------------------------------------------------------*/
	/** This string buffer receives error messages while the document gets parsed. */
	private StringBuffer	m_xmlerr_sb;


	/**
	 * Creates a XERXES DOM parser, parses the document, and returns the DOM
	 * associated with the thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	public Document getDocument(InputStream is, String ident) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		m_xmlerr_sb = new StringBuffer();
		try {
			//-- Assign myself as the error handler for parsing,
			db.setErrorHandler(this);
			InputSource ins = new InputSource(is);
			if(ident != null)
				ins.setPublicId(ident);
			Document doc = db.parse(ins);

			//-- If we're already in trouble now then exit with an exception.
			if(m_xmlerr_sb.length() > 0)
				throw new Exception("Input document parse exceptions: " + m_xmlerr_sb.toString());
			return doc;
		} catch(IOException x) {
			throw new IOException("XML Parser IO error on " + ident + ": " + x.toString());
		}

		//-- Create a DOM parser.
		//		DOMParser	parser	= new DOMParser();
		//		m_xmlerr_sb	= new StringBuffer();
		//		try
		//		{
		//			//-- Assign myself as the error handler for parsing,
		//			parser.setErrorHandler(this);
		//			parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
		//			InputSource	ins = new InputSource(is);
		//			if(ident != null) ins.setPublicId(ident);
		//			parser.parse(ins);
		//			Document	doc	= parser.getDocument();
		//
		//			//-- If we're already in trouble now then exit with an exception.
		//			if(m_xmlerr_sb.length() > 0)
		//				throw new Exception("Input document parse exceptions: "+m_xmlerr_sb.toString());
		//			return doc;
		//		}
		//		catch(IOException x)
		//		{
		//			throw new IOException("XML Parser IO error on "+ident+": "+x.toString());
		//		}
	}

	/**
	 * Creates a XERXES DOM parser, parses the document, and returns the DOM
	 * associated with the thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	public Document getDocument(File inf) throws Exception {
		return getDocument(inf, true);
	}

	/**
	 * Creates a XERXES DOM parser, parses the document, and returns the DOM
	 * associated with the thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	public Document getDocument(File inf, boolean nsaware) throws Exception {
		//-- Create a DOM parser.
		if(!inf.exists() || !inf.isFile())
			throw new IOException(inf + ": file not found.");
		inf = inf.getAbsoluteFile();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(nsaware);
		DocumentBuilder db = dbf.newDocumentBuilder();
		m_xmlerr_sb = new StringBuffer();
		try {
			//-- Assign myself as the error handler for parsing,
			db.setErrorHandler(this);
			Document doc = db.parse(inf);

			//-- If we're already in trouble now then exit with an exception.
			if(m_xmlerr_sb.length() > 0)
				throw new Exception("Input document parse exceptions: " + m_xmlerr_sb.toString());
			return doc;
		} catch(IOException x) {
			throw new IOException("XML Parser IO error on " + inf + ": " + x.toString());
		}
	}


	private void genErr(SAXParseException exception, String type) {
		if(m_xmlerr_sb.length() > 0)
			m_xmlerr_sb.append("\n");
		m_xmlerr_sb.append("req.xml(");
		m_xmlerr_sb.append(Integer.toString(exception.getLineNumber()));
		m_xmlerr_sb.append(':');
		m_xmlerr_sb.append(Integer.toString(exception.getColumnNumber()));
		m_xmlerr_sb.append(") ");
		m_xmlerr_sb.append(type);
		m_xmlerr_sb.append(":");
		m_xmlerr_sb.append(exception.getMessage());
	}


	public final void warning(SAXParseException exception) throws SAXException {
		genErr(exception, "warning");
	}

	public final void error(SAXParseException exception) throws SAXException {
		genErr(exception, "error");
	}

	public final void fatalError(SAXParseException exception) throws SAXException {
		genErr(exception, "fatal");
	}

	public String getErrors() {
		return m_xmlerr_sb.toString();
	}

	/**
	 * Can be called to walk all items in an XML node. It locates the named method in
	 * the class and calls the method. If the method cannot be found it throws an exception.
	 * @param nd
	 * @param method
	 */
	static public void nodeWalker(Node nd, Object callobj, String method, Object[] ar) throws Exception {
		int al = ar == null ? 0 : ar.length;
		Class cl = callobj.getClass();
		Method m = findMethod(cl, method, al + 1);
		if(m == null)
			throw new IllegalStateException("Class " + cl.getName() + " does not contain method " + method);
		Object[] par = new Object[al + 1];
		if(ar != null) {
			for(int i = ar.length; --i >= 0;)
				par[i + 1] = ar[i];
		}

		//-- Walk the node
		NodeList nl = nd.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if(!item.getNodeName().equals("#text")) {
				par[0] = item;
				try {
					m.invoke(callobj, par);
				} catch(InvocationTargetException x) {
					Throwable t = x.getTargetException();
					if(t instanceof Exception)
						throw (Exception) x.getTargetException();
					throw x;
				}
			}
		}
	}

	/**
	 * Decodes an XML node and calls a specific setter method in the specified object for 
	 * all nodes encountered in the method. With a null xlist if a node is found with the
	 * name 'Abcd':
	 * <ul>
	 *	<li>The function startAbcd is located and called if found (see call data below).</li>
	 *	<li>For each attribute 'Xyz' of Abcd the function attrAbcdXyz gets called with the
	 *		attribute data.</li>
	 *	<li>The function inAbcd is located and called if found.</li>
	 * </ul>
	 * 
	 * If the parameter namemap is present it maps input names (xml node names) to function
	 * names. This can be used to translate xml names.
	 *
	 * @param nd
	 * @param callobj
	 * @param xlist
	 * @throws Exception
	 */
	static public void nodeSetCaller(Node nd, Object callobj, Hashtable namemap) throws Exception {
		//-- Walk the node
		NodeList nl = nd.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			String key = item.getNodeName(); // The function's name key by default
			if(namemap != null)
				key = (String) namemap.get(key); // If a translate table is present then translate
			else
				key = translateKey(key); // If direct translate to remove - and such
			if(key != null) // A name was found -> find method
			{
				if(key.equals("#text"))
					key = "NodeText";
				handleNode(item, callobj, key); // Handle this entire node
			}
		}

		//-- Call a check handler if defined
		try {
			Class cl = callobj.getClass();
			Method m = findMethod(cl, "xmlTagEnd", -1);
			if(m != null)
				callMethod(nd, m, callobj);
		} catch(XmlErrorException x) {
			if(x.getTag() == null)
				x.setTag(nd);
			throw x;
		}
	}

	static private String translateKey(String k) {
		int len = k.length();
		StringBuffer sb = new StringBuffer(len);
		boolean upcase = true;
		for(int i = 0; i < len; i++) {
			char c = k.charAt(i);
			if(c == '-')
				upcase = true;
			else {
				if(upcase) {
					c = Character.toUpperCase(c);
					upcase = false;
				}
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Handles all calls for a node.
	 * @param nd			The node that is to be used
	 * @param callobj		The object on which the call is to be located
	 * @param key			The base function name to use, without prefixes. This is usually
	 * 						the Java-ised node name, i.e. for a node test-config this is TestConfig.
	 */
	static private void handleNode(Node nd, Object callobj, String key) throws Exception {
		try {
			if(!handleNodeCall(nd, callobj, key, "start")) // Call startTestConfig
			{
				if(!handleNodeCall(nd, callobj, key, "set")) // call setTestConfig
				{
					Method m = findUndefined(callobj, "set");
					if(m != null)
						callMethod(nd, m, callobj);
				}
			}
			//			handleAttrCall(nd, callobj, key);					// call setTestConfigXXX to set attributes
			handleNodeCall(nd, callobj, key, "end"); // Call endTestConfig (check for errors)
		} catch(XmlErrorException xe) {
			if(xe.getTag() == null)
				xe.setTag(nd);
			throw xe;
		}
	}

	static private boolean handleNodeCall(Node nd, Object callobj, String key, String prefix) throws Exception {
		Method m = findElementCall(callobj, key, prefix);
		if(m == null)
			return false;
		callMethod(nd, m, callobj);
		return true;
	}

	/**
	 * Calls setter methods for all of the attributes in a node.
	 * @param n
	 * @param callobj
	 * @param prefix
	 * @throws Exception
	 */
	static public void attrSetCaller(Node n, Object callobj, String prefix) throws Exception {
		handleAttrCall(n, callobj, prefix);
	}


	/**
	 * Tries to call setters for all attributes in a thingy... This tries specific
	 * attribute setters first, then it tries normal setters. 
	 * @param nd
	 * @param callobj
	 * @param key
	 * @throws Exception
	 */
	static private void handleAttrCall(Node nd, Object callobj, String key) throws Exception {
		//-- Walk all attributes and call their method functor.
		NamedNodeMap map = nd.getAttributes();
		if(map == null)
			return;
		Node attr = null;
		for(int i = 0; i < map.getLength(); i++) {
			attr = map.item(i);

			try {
				String attrkey = translateKey(attr.getNodeName()); // The base name for this attribute
				Method m = findAttrCallMethod(callobj, attrkey, key); // Try to find a method (including undefined)
				if(m != null)
					callMethod(attr, m, callobj);
			} catch(XmlErrorException xe) {
				xe.setAttr(attr);
				throw xe;
			}
		}
	}

	static private Method findAttrCallMethod(Object callobj, String attrkey, String nodenamekey) {
		//-- 1. Try set[Nodekey][AttrKey]
		Class cl = callobj.getClass();
		Method m = findMethod(cl, "set" + nodenamekey + attrkey, -1);
		if(m != null)
			return m;

		//-- Try set[attrkey]
		m = findMethod(cl, "set" + attrkey, -1);
		if(m != null)
			return m;

		//-- set[Nodekey]Undefined
		m = findMethod(cl, "set" + nodenamekey + "Undefined", -1);
		if(m != null)
			return m;

		//-- Try setUndefined
		m = findMethod(cl, "setUndefined", -1);
		return m;
	}


	/**
	 * Tries to locate a method call in the callobj specified for
	 * a tag call.
	 * @param callobj
	 * @param key
	 * @return
	 */
	static private Method findElementCall(Object callobj, String key, String prefix) {
		String name = prefix + key;
		Class cl = callobj.getClass();
		return findMethod(cl, name, -1);
	}

	static private Method findUndefined(Object callobj, String prefix) {
		//-- No method for this. Can we find [prefix]Undefined?
		Class cl = callobj.getClass();
		return findMethod(cl, prefix + "Undefined", -1);
	}

	/**
	 * Call the handler method specified for the node.
	 * @param nd
	 * @param m
	 * @param callobj
	 */
	static private void callMethod(Node nd, Method m, Object callobj) throws Exception {
		//		System.err.println("#xml: calling "+m.getName()+" on "+callobj.getClass().getName()+": "+callobj);

		//-- We have a method and need to find parameters..
		Class[] par = m.getParameterTypes();
		Object[] obj = new Object[par.length]; // For param values.
		for(int i = par.length; --i >= 0;) {
			String txt = DomTools.textFrom(nd);

			if(par[i] == Node.class)
				obj[i] = nd;
			else if(par[i] == String.class)
				obj[i] = txt;
			else if(par[i] == Integer.TYPE) {
				try {
					obj[i] = new Integer(txt.trim());
				} catch(Exception x) {
					throw new Exception(nd.getNodeName() + ": must contain a valid integer number but contains '" + txt + "'");
				}
			} else if(par[i] == Boolean.TYPE) {
				obj[i] = Boolean.valueOf(DomTools.decodeBoolStr(txt, nd.getNodeName()));
			} else
				throw new Exception(m + ": cannot provide value for parameter " + i + ": " + par[i].getName());

			//			System.out.println("  call: set param "+i+" type "+par[i].getName()+" to "+obj[i]);
		}

		//-- Invoke
		try {
			//			System.out.println("xml: call "+m+" using "+obj+" on "+callobj);
			m.invoke(callobj, obj);
		} catch(InvocationTargetException x) {
			Throwable t = x.getTargetException();
			if(t instanceof Exception)
				throw (Exception) x.getTargetException();
			throw x;
		}
	}

	//	
	//	/**
	//	 * Tries to find a handler routine in the class specified.
	//	 * @param nd
	//	 * @param callobj
	//	 * @param key
	//	 * @param prefix
	//	 * @return			TRUE if a call was placed, FALSE if no method was found.
	//	 * @throws Exception
	//	 */
	//	static private boolean	handleNodeCall(Node nd, Object callobj, String key, String prefix) throws Exception
	//	{
	//		String	name = prefix + key;
	//		Class	cl	= callobj.getClass();
	//		Method	m = findMethod(cl, name, -1);
	//		if(m == null)
	//		{
	//			//-- No method for this. Can we find [prefix]Undefined?
	//			m	= findMethod(cl, prefix+"Undefined", -1);
	//			if(m == null)
	//				return false;
	//		}
	//		
	//		//-- We have a method and need to find parameters..
	//		Class[]		par	= m.getParameterTypes();
	//		Object[]	obj = new Object[par.length];			// For param values.
	//		for(int i = par.length; --i >= 0;)
	//		{
	//			String txt = DomTools.textFrom(nd);
	//			
	//			if(par[i] == Node.class)
	//				obj[i] = nd;
	//			else if(par[i] == String.class)
	//				obj[i] = txt;
	//			else if(par[i] == Integer.TYPE)
	//			{
	//				try
	//				{
	//					obj[i] = new Integer(txt.trim());
	//				}
	//				catch(Exception x)
	//				{
	//					throw new Exception(nd.getNodeName()+": must contain a valid integer number but contains '"+txt+"'");
	//				}
	//			}
	//			else if(par[i] == Boolean.TYPE)
	//			{
	//				obj[i] = Boolean.valueOf(DomTools.decodeBoolStr(txt, nd.getNodeName()));
	//			}
	//			else
	//				throw new Exception(m+": cannot provide value for parameter "+i+": "+par[i].getName());
	//			
	////			System.out.println("  call: set param "+i+" type "+par[i].getName()+" to "+obj[i]);
	//		}
	//		
	//		//-- Invoke
	//		try
	//		{
	////			System.out.println("xml: call "+m+" using "+obj+" on "+callobj);
	//			m.invoke(callobj, obj);
	//			return true;
	//		}
	//		catch(InvocationTargetException x)
	//		{
	//			Throwable t = x.getTargetException();
	//			if(t instanceof Exception)
	//				throw (Exception)x.getTargetException();
	//			throw x;
	//		}
	//	}

	static private HashMap	m_method_ht	= new HashMap();

	static private Method findMethod(Class cl, String name, int nparam) {
		//		System.out.println("   find "+name+" in "+cl.getName());
		String k = cl.getName() + "#" + name + nparam;
		Object o = m_method_ht.get(k);
		if(o != null)
			return (Method) o;

		Method[] mar = cl.getMethods();
		for(int i = mar.length; --i >= 0;) {
			Method m = mar[i];
			if(m.getName().equals(name)) {
				if(nparam == -1 || m.getParameterTypes().length == nparam) {
					m_method_ht.put(k, m);
					return m;
				}
			}
		}
		return null;
	}

	static public String nodePath(Node n) {
		StringBuffer sb = new StringBuffer();
		nodePath(sb, n);
		return sb.toString();
	}

	static public void nodePath(StringBuffer sb, Node n) {
		if(n.getParentNode() != null) {
			nodePath(sb, n.getParentNode());
			sb.append("->");
		}
		sb.append(n.getNodeName());
	}

	static public void error(String msg) throws XmlErrorException {
		throw new XmlErrorException(msg);
	}
}
