package to.etc.webapp.nls;

import java.text.*;
import java.util.*;

/**
 *
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 28, 2007
 */
public class NlsContext {
	/**
	 * All of the bundleRefs currently used, indexed by bundle key.
	 */
	static private Map<String, BundleRef> m_refMap = new HashMap<String, BundleRef>();

	static private String m_dialect;

	private NlsContext() {}

	/**
	 * The thingy holding the current locale per thread. If the thingy is null
	 * the current locale is unknown.
	 */
	static private final ThreadLocal<Locale> m_currentLocale = new ThreadLocal<Locale>();

	/**
	 * Returns the default ViewPoint locale. <b>DO NOT USE!!!!</b>, except when absolutely necessary! To get
	 * the actual locale that is being used by a request call getLocale()!
	 * @return
	 */
	static public Locale getDefault() {
		return Locale.getDefault();
	}

	static public String getDialect() {
		return m_dialect;
	}

	/**
	 * Gets the current locale in use by the request we're executing at this time. This is
	 * the ONLY call that may be executed from normal user code.
	 * @return
	 */
	static public Locale getLocale() {
		Locale loc = m_currentLocale.get();
		if(loc == null)
			return getDefault();
		return loc;
	}

	/**
	 * Sets the current locale. <b>DO NOT USE!!!!!</b> This method should ONLY be
	 * called from system code!
	 * @param loc
	 */
	static public void setLocale(final Locale loc) {
		m_currentLocale.set(loc);
	}

	/*--------------------------------------------------------------*/
	/* CODING: Locale-specific message retrieval                    */
	/*--------------------------------------------------------------*/
	/**
	 * Get the BundleRef for the specified bundle key. This represents a given set of
	 * messages for all possible locales.
	 * @param bundlekey     The key (package/filename) of the resource bundle without .properties!
	 * @return
	 */
	static synchronized public BundleRef getBundleRef(final String bundlekey) {
		BundleRef r = m_refMap.get(bundlekey);
		if(r == null) {
			r = new BundleRef(bundlekey);
			m_refMap.put(bundlekey, r);
		}
		return r;
	}

	static public DateFormat getDateFormat(final int style) {
		return DateFormat.getDateInstance(style, NlsContext.getLocale());
	}

	static public DateFormat getDateTimeFormat(final int s1, final int s2) {
		return DateFormat.getDateTimeInstance(s1, s2, NlsContext.getLocale());
	}

	/*--------------------------------------------------------------*/
	/* CODING: Globalized message management (bundle pools).        */
	/*--------------------------------------------------------------*/
	static private NlsMessageProviderList m_msgProvList = new NlsMessageProviderList();

	static private NlsCachingMessageProvider m_cp = new NlsCachingMessageProvider(m_msgProvList);

	/**
	 * Walks all registered message providers and gets the most appropriate message. If
	 * the message has been provided before it will be returned.
	 *
	 * @param loc
	 * @param code
	 * @return
	 */
	static public String findGlobalMessage(final Locale loc, final String code) {
		return m_cp.findMessage(loc, code);
	}

	static public String getGlobalMessage(final String code, final Object... param) {
		Locale loc = getLocale();
		String msg = findGlobalMessage(loc, code);
		if(msg == null)
			return "???" + code + "???";
		MessageFormat temp = new MessageFormat(msg, loc);
		return temp.format(param);
	}

	/**
	 * DO NOT USE GLOBAL BUNDLES ANYMORE!!
	 * @param bundlekey
	 */
	@Deprecated
	static public void registerBundle(final String bundlekey) {
		m_msgProvList.addProvider(getBundleRef(bundlekey));
	}


	/**
	 * Checks if this object is a resource key specificator (~key~) and replaces
	 * it if so by looking up the global message. This only gets the message; it
	 * does not pass it through MessageFormat.format()!!
	 * @param in
	 * @return
	 */
	static public Object replaceResource(final Object in) {
		if(in == null || !(in instanceof String))
			return null;
		return replaceResource((String) in);
	}

	/**
	 * Checks if this string is a resource key specificator (~key~) and replaces
	 * it if so by looking up the global message. This only gets the message; it
	 * does not pass it through MessageFormat.format()!!
	 * @param in
	 * @return
	 */
	static public String replaceResource(final String s) {
		if(s == null || s.length() < 3) // Too small- exit
			return s;
		if(s.charAt(0) != '~')
			return s;
		if(s.charAt(s.length() - 1) != '~')
			return s;
		if(s.charAt(1) == '~') // is ~~bleh~ -> replace to ~bleh~
			return s.substring(1);

		//-- This is a replacement expression - replace
		Locale loc = getLocale();
		String msg = findGlobalMessage(loc, s.substring(1, s.length() - 1));
		return msg == null ? s : msg;
	}

	/*--------------------------------------------------------------*/
	/* CODING: Static code table message/description replacement.   */
	/*--------------------------------------------------------------*/
	/**
	 * Create a coded translation table for code tables.
	 *
	 * FIXME Needs real implementation.
	 */
	static public NlsCodeTable createCodeTable(final String name) {
		return new NlsCodeTable(name);
	}


	/**
	 * FIXME This needs to use a cached code translation table from the database.
	 */
	static public String getCodeDescription(final String tablename, final String code, final String defmsg) {
		return defmsg;
	}

	/**
	 * Register default global bundles for this project
	 */
	static {
		registerBundle("to.etc.domui.util.messages");
	}

}
