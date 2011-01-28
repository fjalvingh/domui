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
package to.etc.webapp.nls;

import java.text.*;
import java.util.*;

/**
 * <p>Static accessor class to the current locale, "jargon" and currency information. This
 * class behaves as a static class but has <b>threadlocal based storage</b> behind it; this allows
 * the locale information to be different for every request. The actual locale to use for
 * a request can be set by a server on a per-request basis. If no per-request locale is set
 * this uses the default locale.</p>
 * <p>In addition, this also contains the current "currency locale" which is set once per
 * application, when used. By default most applications will contain monetary amounts in a
 * single currency only, regardless of the language they are being used in. For instance a
 * dutch system can present information in English for a british user, but will still present
 * amounts in euro's, not pounds.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 28, 2007
 */
final public class NlsContext {
	/**
	 * All of the bundleRefs currently used, indexed by bundle key.
	 */
	static private Map<String, BundleRef> m_refMap = new HashMap<String, BundleRef>();

	static private String m_dialect;

	/** The default currency locale; used when no currency is explicitly set. */
	static private Locale m_defaultCurrencyLocale;

	/**
	 * The thingy holding the current locale per thread. If the thingy is null
	 * the current locale is unknown and the default locale will be returned, mirroring
	 * the behaviour of the badly defined standard java sdk libs.
	 */
	static private final ThreadLocal<Locale> m_currentLocale = new ThreadLocal<Locale>();

	/**
	 * The locale for the currency being handled by the request. This defaults to the "default locale".
	 */
	static private final ThreadLocal<Locale> m_currencyLocale = new ThreadLocal<Locale>();

	/**
	 * The Currency instance for the default application-wide currency.
	 */
	static private final ThreadLocal<Currency> m_currency = new ThreadLocal<Currency>();

//	static private Locale m_currencyLocale;

//	static private Currency m_currency;

//	/**
//	 * Contains the symbol representing a currency because the currency class is as usual too bloody stupid
//	 * to return an euro sign as it should.
//	 */
//	static private String m_currencySymbol;

	/**
	 * Make sure this is never constructed.
	 */
	private NlsContext() {}


	/**
	 * Returns the default ViewPoint locale. <b>DO NOT USE!!!!</b>, except when absolutely necessary! To get
	 * the actual locale that is being used by a request call getLocale()!
	 * @return
	 */
	static public Locale getDefault() {
		return Locale.getDefault();
	}

	public synchronized static void setDefaultCurrencyLocale(Locale defaultCurrencyLocale) {
		m_defaultCurrencyLocale = defaultCurrencyLocale;
	}

	public synchronized static Locale getDefaultCurrencyLocale() {
		return m_defaultCurrencyLocale == null ? getDefault() : m_defaultCurrencyLocale;
	}

	/**
	 * <p>The locale for the currency being handled in the application. Can be changed per request,
	 * and defaults to the "default locale" (NOT the per request locale!). This
	 * is used for applications that use only a single currency throughout the application; if
	 * an application uses multiple currencies the application itself must provide services to
	 * handle the "current" currency and monetary conversions of currencies.</p>
	 * <p>This will return the <i>default locale</i> if not explicitly set.</p>
	 * @return
	 */
	static public Locale getCurrencyLocale() {
		Locale loc = m_currencyLocale.get();
		return loc == null ? getDefaultCurrencyLocale() : loc;
	}

	/**
	 * Sets the currency locale to use for this request.
	 * @param loc
	 */
	static public void setCurrencyLocale(Locale loc) {
		m_currencyLocale.set(loc);
		m_currency.set(Currency.getInstance(loc));
	}

	/**
	 * Returns a Currency object for the current currency locale.
	 * @return
	 */
	static public Currency getCurrency() {
		Currency c = m_currency.get();
		if(c != null)
			return c;
		c = Currency.getInstance(getCurrencyLocale());
		m_currency.set(c);
		return c;
	}

	/**
	 * Returns the currency symbol (not the currency code, damnit) for the current currency locale. This will
	 * return the euro sign € instead of EUR.
	 * @return
	 */
	static public String getCurrencySymbol() {
		Currency c = getCurrency();
		String s = c.getSymbol(getLocale());
		if("EUR".equals(s))
			s = "\u20ac";
		return s;
	}

	static public synchronized String getDialect() {
		return m_dialect;
	}

	public static synchronized void setDialect(String dialect) {
		m_dialect = dialect;
	}

	/**
	 * Gets the current locale in use by the request we're executing at this time - this is
	 * the PROPER call to use from normal user code.
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
	 * called from system (server) code! It sets the per-request locale.
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
	//	static private NlsMessageProviderList m_msgProvList = new NlsMessageProviderList();

	//	static private NlsCachingMessageProvider m_cp = new NlsCachingMessageProvider(m_msgProvList);

	//	/**
	//	 * ALL NEW USE EXPRESSLY FORBIDDEN - PENDING DELETION.
	//	 * Walks all registered message providers and gets the most appropriate message. If
	//	 * the message has been provided before it will be returned.
	//	 *
	//	 * @param loc
	//	 * @param code
	//	 * @return
	//	 */
	//	@Deprecated
	//	static public String findGlobalMessage(final Locale loc, final String code) {
	//		return m_cp.findMessage(loc, code);
	//	}

	/**
	 * ALL NEW USE EXPRESSLY FORBIDDEN - PENDING DELETION.
	 * @param code
	 * @param param
	 * @return
	 */
	//	@Deprecated
	//	static public String getGlobalMessage(final String code, final Object... param) {
	//		Locale loc = getLocale();
	//		String msg = findGlobalMessage(loc, code);
	//		if(msg == null)
	//			return "???" + code + "???";
	//		MessageFormat temp = new MessageFormat(msg, loc);
	//		return temp.format(param);
	//	}

	//	/**
	//	 * ALL NEW USE EXPRESSLY FORBIDDEN - PENDING DELETION.
	//	 * DO NOT USE GLOBAL BUNDLES ANYMORE!!
	//	 * @param bundlekey
	//	 */
	//	@Deprecated
	//	static public void registerBundle(final String bundlekey) {
	//		m_msgProvList.addProvider(getBundleRef(bundlekey));
	//	}
	//

	//	/**
	//	 * ALL NEW USE EXPRESSLY FORBIDDEN - PENDING DELETION (GLOBAL MESSAGE BUNDLES).
	//	 * Checks if this object is a resource key specificator (~key~) and replaces
	//	 * it if so by looking up the global message. This only gets the message; it
	//	 * does not pass it through MessageFormat.format()!!
	//	 * @param in
	//	 * @return
	//	 */
	//	@Deprecated
	//	static public Object replaceResource(final Object in) {
	//		if(in == null || !(in instanceof String))
	//			return null;
	//		return replaceResource((String) in);
	//	}

	//	/**
	//	 * ALL NEW USE EXPRESSLY FORBIDDEN - PENDING DELETION (GLOBAL MESSAGE BUNDLES).
	//	 * Checks if this string is a resource key specificator (~key~) and replaces
	//	 * it if so by looking up the global message. This only gets the message; it
	//	 * does not pass it through MessageFormat.format()!!
	//	 * @param in
	//	 * @return
	//	 */
	//	@Deprecated
	//	static public String replaceResource(final String s) {
	//		if(s == null || s.length() < 3) // Too small- exit
	//			return s;
	//		if(s.charAt(0) != '~')
	//			return s;
	//		if(s.charAt(s.length() - 1) != '~')
	//			return s;
	//		if(s.charAt(1) == '~') // is ~~bleh~ -> replace to ~bleh~
	//			return s.substring(1);
	//
	//		//-- This is a replacement expression - replace
	//		Locale loc = getLocale();
	//		String msg = findGlobalMessage(loc, s.substring(1, s.length() - 1));
	//		return msg == null ? s : msg;
	//	}

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

	//	/**
	//	 * Register default global bundles for this project
	//	 */
	//	static {
	//		registerBundle("to.etc.domui.util.messages");
	//	}

}
