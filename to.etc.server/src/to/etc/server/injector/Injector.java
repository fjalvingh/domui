package to.etc.server.injector;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import javax.servlet.*;

import org.w3c.dom.*;

import to.etc.xml.*;

/**
 * This maintains a set of Injector providers. Each provider is able
 * to generate a "setter" for a given pair of (Source Object, Target type/name).
 * 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 11, 2006
 */
public class Injector {
	static public final String				NOVALUE					= "{{%no$val#}}";

	static private final Injector			m_instance				= new Injector();

	static final public Annotation[]		NO_ANNOTATIONS			= new Annotation[0];

	private List<InjectorConverterFactory>	m_paramConverterList	= new ArrayList<InjectorConverterFactory>();

	private List<RetrieverProvider>			m_retrieverProviderList	= new ArrayList<RetrieverProvider>();

	public Injector() {
		addParameterConverter(EnumParamConverter.FACTORY);
		addParameterConverter(IntParamConverter.FACTORY);
		addParameterConverter(LongParamConverter.FACTORY);
		addParameterConverter(new XMLNodeInjectorConverter());
		addParameterConverter(new DateParamConverterFactory());
		addParameterConverter(new ObjectConverterFactory());
		addParameterConverter(new BooleanParamConverterFactory());
		//		addParameterProvider(StringProvider.FACTORY);
	}

	/**
	 * Don't use anymore. Injectors are dependent on context.
	 * @deprecated
	 * @return
	 */
	@Deprecated
	static public Injector getInstance() {
		return m_instance;
	}

	/**
	 * Utility method to initialize this injector from a servlet. This gets the 
	 * context parameters and the servlet parameters that have to do
	 * with default parameter injection.
	 *
	 * @param cf
	 */
	public synchronized void initFromServlet(ServletConfig cf) throws ServletException {
		String param = cf.getInitParameter("default-parameter-converters");
		if(param != null)
			addConverterList(param);
		param = cf.getServletContext().getInitParameter("default-parameter-converters");
		if(param != null)
			addConverterList(param);
		param = cf.getInitParameter("default-retriever-providers");
		if(param != null)
			addRetrieverList(param);
		param = cf.getServletContext().getInitParameter("default-retriever-providers");
		if(param != null)
			addRetrieverList(param);
	}

	/*--------------------------------------------------------------*/
	/* CODING: Parameter provider code.                             */
	/*--------------------------------------------------------------*/
	public synchronized void addParameterConverter(InjectorConverterFactory pp) {
		//-- Use copy-and-replace to prevent the list from changing while being accessed
		for(InjectorConverterFactory tp : m_paramConverterList) {
			if(tp.getClass() == pp.getClass()) // Prevent duplicates
				return;
		}
		List<InjectorConverterFactory> newl = new ArrayList<InjectorConverterFactory>(m_paramConverterList);
		newl.add(pp);
		m_paramConverterList = newl;
	}

	private synchronized List<InjectorConverterFactory> getParamConverterList() {
		return m_paramConverterList;
	}

	public InjectorConverter findParameterConverter(Class totype, Class fromtype) throws Exception {
		return findParameterConverter(totype, fromtype, NO_ANNOTATIONS);
	}

	public InjectorConverter findParameterConverter(Class totype, Class fromtype, Annotation[] anns) throws Exception {
		List<InjectorConverterFactory> list = getParamConverterList();
		for(InjectorConverterFactory p : list) {
			InjectorConverter pp = p.accepts(totype, fromtype, anns);
			if(pp != null)
				return pp;
		}
		return null;
	}

	public void addConverterList(String lst) throws ServletException {
		StringTokenizer st = new StringTokenizer(lst, " \t,");
		while(st.hasMoreTokens()) {
			String s = st.nextToken().trim();
			if(s.length() > 0) {
				InjectorConverterFactory p = loadConverter(s);
				if(p instanceof XmlParameterizable)
					throw new IllegalStateException("The injector converter '" + s + "' requires parameters.");
				addParameterConverter(p);
			}
		}
	}

	private InjectorConverterFactory loadConverter(String name) throws ServletException {
		//-- 1. Try to load this class,
		//		System.out.println("init: AjaxServer loading parameter converter "+name);
		Class cl = tryClass(name);
		if(cl == null)
			throw new ServletException("The default-parameter-converter class '" + name + "' cannot be found");
		if(!InjectorConverterFactory.class.isAssignableFrom(cl))
			throw new ServletException("The default-parameter-converter class '" + name + "' does not implement " + InjectorConverterFactory.class.getCanonicalName());
		try {
			InjectorConverterFactory p = (InjectorConverterFactory) cl.newInstance();
			return p;
		} catch(Exception x) {
			throw new ServletException("The default-parameter-converter class '" + name + "' could not be instantiated: " + x);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING: XML config reader.									*/
	/*--------------------------------------------------------------*/

	public void loadConfig(File f) throws Exception {
		Document doc = DomTools.getDocument(f, false);
		if(doc == null)
			return;
		Node n = DomTools.getRootElement(doc);
		loadConfig(n);
	}

	public synchronized void loadConfig(Node inn) throws Exception {
		if(!inn.getNodeName().equals("ioc"))
			throw new Exception("Expecting 'ioc' node as root element");
		NodeList nl = inn.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if(n.getNodeName().equals("#text"))
				continue;
			handleNode(n);
		}
	}

	/**
	 * Decode a name and define.
	 * @param inn
	 * @throws Exception
	 */
	private void handleNode(Node inn) throws Exception {
		if(inn.getNodeName().equals("retriever"))
			decodeRetriever(inn);
		else if(inn.getNodeName().equals("converter"))
			decodeConverter(inn);
		else
			throw new Exception("Unexpected node '" + inn.getNodeName() + "'");
	}

	/**
	 * Decodes a retriever. The only mandatory attribute is 'class'.
	 *
	 * @param inn
	 * @throws Exception
	 */
	private void decodeRetriever(Node inn) throws Exception {
		String cn = DomTools.strAttr(inn, "class");
		if(cn == null)
			throw new Exception("Missing 'class' attribute on " + inn.getNodeName() + " node");
		RetrieverProvider p = loadRetrieverClass(cn); // Loads or an exception occurs.
		if(p instanceof XmlParameterizable) { // Can be configured?
			((XmlParameterizable) p).configure(inn); // Yes-> configure
		} else {
			NamedNodeMap m = inn.getAttributes();
			if(m.getLength() != 1)
				throw new Exception("The retriever-factory with class=" + cn + " does NOT accept parameters (attributes).");
		}
		addRetrieverProvider(p);
	}

	/**
	 * Decodes a converter. The only mandatory attribute is 'class'.
	 * @param inn
	 * @throws Exception
	 */
	private void decodeConverter(Node inn) throws Exception {
		String cn = DomTools.strAttr(inn, "class");
		if(cn == null)
			throw new Exception("Missing 'class' attribute on " + inn.getNodeName() + " node");
		InjectorConverterFactory f = loadConverter(cn); // Loads or an exception occurs.
		if(f instanceof XmlParameterizable) { // Can be configured?
			((XmlParameterizable) f).configure(inn); // Yes-> configure
		} else {
			NamedNodeMap m = inn.getAttributes();
			if(m.getLength() != 1)
				throw new Exception("The injector-converter with class=" + cn + " does NOT accept parameters (attributes).");
		}
		addParameterConverter(f);
	}

	/*-------------------------------------------------------------*/
	/* CODING: retriever provider.                                 */
	/*-------------------------------------------------------------*/

	public synchronized void addRetrieverProvider(RetrieverProvider rp) {
		for(RetrieverProvider trp : m_retrieverProviderList) {
			if(trp == rp)
				return;
		}
		List<RetrieverProvider> newl = new ArrayList<RetrieverProvider>(m_retrieverProviderList);
		newl.add(rp);
		m_retrieverProviderList = newl;
	}

	synchronized List<RetrieverProvider> getRetrieverProviderList() {
		return m_retrieverProviderList;
	}

	public void addRetrieverList(String lst) throws ServletException {
		StringTokenizer st = new StringTokenizer(lst, " \t,");
		while(st.hasMoreTokens()) {
			String s = st.nextToken().trim();
			if(s.length() > 0) {
				RetrieverProvider p = loadRetrieverClass(s);
				if(p instanceof XmlParameterizable)
					throw new IllegalStateException("The retriever factory '" + s + "' requires parameters.");
				addRetrieverProvider(p);
			}
		}
	}

	private RetrieverProvider loadRetrieverClass(String name) throws ServletException {
		//-- 1. Try to load this class,
		//        System.out.println("injector: loading retriever provider "+name);
		Class cl = tryClass(name);
		if(cl == null)
			throw new ServletException("The retriever-provider class '" + name + "' cannot be found");
		if(!RetrieverProvider.class.isAssignableFrom(cl))
			throw new ServletException("The retriever-provider class '" + name + "' does not implement " + RetrieverProvider.class.getCanonicalName());
		try {
			RetrieverProvider p = (RetrieverProvider) cl.newInstance();
			return p;
		} catch(Exception x) {
			throw new ServletException("The retriever-provider class '" + name + "' could not be instantiated: " + x);
		}
	}

	/**
	 * Walk the retriever providers and ask for a retriever for the given source.
	 *
	 * @param sourcecl
	 * @param name
	 * @param pann
	 * @return
	 */
	public Retriever findRetriever(Class sourcecl, Class targetcl, String name, Annotation[] pann) {
		//    	System.out.println("injector: finding retriever for "+targetcl+" name="+name+" from source="+sourcecl);
		for(RetrieverProvider rp : m_retrieverProviderList) {
			Retriever r = rp.makeRetriever(sourcecl, targetcl, name, pann);
			if(r != null) {
				//            	System.out.println("injector: retriever found @ provider "+rp);
				return r;
			} //else
			//System.out.println("injector: rejected by provider "+rp);
		}
		//    	System.out.println("injector: retriever not found");
		return null;
	}

	/*--------------------------------------------------------------*/
	/* CODING:  Utility functions.                          		*/
	/*--------------------------------------------------------------*/
	static private Class tryClass(String name) {
		try {
			return Class.forName(name);
		} catch(Exception z) {
			return null;
		}
	}

	/**
	 * This utility function walks all methods off a class and determines
	 * all setters from it. It returns a map of [setter name, setter method]
	 * for all valid setters found.
	 *
	 * @param cl
	 * @return
	 */
	static public Map<String, Method> getObjectSetterMap(Class cl) {
		Map<String, Method> map = new HashMap<String, Method>();
		Method[] mar = cl.getMethods();
		for(Method m : mar) {
			int mod = m.getModifiers();
			if(Modifier.isStatic(mod)) // No way we use statics
				continue;
			if(!Modifier.isPublic(mod)) // Not needed I think..
				continue;
			String name = m.getName();
			if(!name.startsWith("set") && name.length() > 3)
				continue;
			Class[] far = m.getParameterTypes(); // Formal  parameter types.
			if(far == null || far.length != 1) // Not single argument?
				continue;

			//-- Calculate a property name.
			StringBuilder sb = new StringBuilder();
			sb.append(name, 3, name.length());
			sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
			name = sb.toString();
			map.put(name, m);
		}
		return map;
	}

	static public String makeMethodName(String prefix, String property) {
		if(property.length() == 0)
			return prefix + "EmptyName";
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(Character.toUpperCase(property.charAt(0)));
		sb.append(property.substring(1));
		return sb.toString();
	}
}
