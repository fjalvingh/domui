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

import java.io.*;
import java.util.*;

import javax.annotation.*;

/**
 * A bundleRef represents a single set of messages in multiple languages. It differs from a ResourceBundle
 * in that a single instantiated ResourceBundle is always for a <i>single</i> language; this makes a ResourceBundle
 * unusable for server based code since every request can be in a different language.
 *
 * <p>BundleRefs specify a code location in the webapp's classpath. The code location consists of a class which
 * defines the "start location" to look for the message files and which determines the basic classloader to use
 * to load those files. BundleRef's are cached globally; when you define the same BundleRef multiple times in the
 * code you will always get a reference to the same bundle. This prevents the same bundle from being reloaded
 * multiple times.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 18, 2006
 */
final public class BundleRef extends BundleBase implements IBundle {
	private ClassLoader m_loader;

	@Nullable
	private final String m_bundleKey;

	private NlsMessageProvider m_parent;

	static private Map<Class< ? >, Map<String, BundleRef>> m_cachedMap = new HashMap<Class< ? >, Map<String, BundleRef>>();

	private final Map<Object, Object> m_map = new HashMap<Object, Object>();

	static public final BundleRef DUMMY = new BundleRef();

	/*--------------------------------------------------------------*/
	/*	CODING:	Creating/accessing BundleRefs.						*/
	/*--------------------------------------------------------------*/

	private BundleRef() {
		m_bundleKey = null;
	}

	/**
	 * Constructor for global BundleRef's.
	 * @param key
	 */
	BundleRef(@Nonnull final String key) {
		m_bundleKey = key;
	}

	private BundleRef(@Nullable final Class< ? > base, @Nonnull final String name) {
		m_loader = null == base ? null : base.getClassLoader();
		m_bundleKey = calcAbsName(base, name);
	}

	private BundleRef(@Nonnull NlsMessageProvider mp) {
		m_bundleKey = null;
		m_parent = mp;
	}

	static public BundleRef createWrapper(@Nonnull NlsMessageProvider mp) {
		return new BundleRef(mp);
	}

	/**
	 * Create or find the BundleRef representing this path. If the ref already exists it gets
	 * returned, else it gets created lazily.
	 * @param clz
	 * @param name Name of the bundle file (minus the ".properties" extension). Case is important!!!
	 * @return
	 */
	@Nonnull
	static public synchronized BundleRef create(@Nonnull final Class< ? > clz, @Nonnull final String name) {
		Map<String, BundleRef> refMap = m_cachedMap.get(clz);
		if(refMap == null) {
			refMap = new HashMap<String, BundleRef>(3);
			m_cachedMap.put(clz, refMap);
		} else {
			BundleRef ref = refMap.get(name);
			if(ref != null)
				return ref;
		}

		//-- Add new ref
		BundleRef ref = new BundleRef(clz, name);
		refMap.put(name, ref);
		return ref;
	}

	static private String calcAbsName(final Class< ? > clz, final String name) {
		if(clz == null)
			return name;
		if(name.startsWith("/"))
			return name.substring(1);
		if(name.endsWith(".properties"))
			throw new IllegalArgumentException("Message bundle name may not include the .properties extension");
		String s = clz.getName(); // Class' name
		int pos = s.lastIndexOf('.');
		if(pos == -1)
			return name;
		return s.substring(0, pos + 1) + name;
	}

	/**
	 * Returns the bundle for the specified locale from cache, and caches
	 * it if not found.
	 * @param loc
	 * @return
	 * @throws ResourceNotFoundException if ...
	 */
	private synchronized ResourceBundle[] getBundleList(final Locale loc) {
		ResourceBundle[] r = (ResourceBundle[]) m_map.get(loc);
		if(r == null) {
			r = loadBundleList(loc); // Load bundle list from classpath
			m_map.put(loc, r);
		}
		return r;
	}

	public boolean exists() {
		if(m_parent != null)
			return true;
		return getBundleList(Locale.US).length != 0;
	}

	@Override
	public String findMessage(final @Nonnull Locale loc, final @Nonnull String code) {
		if(m_parent != null)
			return m_parent.findMessage(loc, code);

		ResourceBundle[] b = getBundleList(loc);
		for(ResourceBundle rb : b) {
			try {
				return rb.getString(code);
			} catch(MissingResourceException x) {}
		}
		return null;
	}

	/**
	 * Overrides the stupid ResourceBundle resolution mechanism. This ignores the
	 * default language. In addition, it re-uses existing bundles.
	 *
	 * @param loc
	 * @return
	 */
	private synchronized ResourceBundle[] loadBundleList(final Locale loc) {
		List<ResourceBundle> rb = new ArrayList<ResourceBundle>();
		tryKey(rb, mkSubKey(loc.getLanguage(), loc.getCountry(), loc.getVariant(), NlsContext.getDialect()));
		tryKey(rb, mkSubKey(loc.getLanguage(), loc.getCountry(), loc.getVariant(), null));
		tryKey(rb, mkSubKey(loc.getLanguage(), loc.getCountry(), null, NlsContext.getDialect()));
		tryKey(rb, mkSubKey(loc.getLanguage(), loc.getCountry(), null, null));
		tryKey(rb, mkSubKey(loc.getLanguage(), null, null, NlsContext.getDialect()));
		tryKey(rb, mkSubKey(loc.getLanguage(), null, null, null));
		tryKey(rb, mkSubKey(null, null, null, NlsContext.getDialect()));
		tryKey(rb, mkSubKey(null, null, null, null));
		return rb.toArray(new ResourceBundle[rb.size()]);
	}

	/**
	 * Try the specified key; if a bundle is found for it add the bundle to the bundle
	 * list.
	 *
	 * @param res
	 * @param key
	 */
	private synchronized void tryKey(final List<ResourceBundle> res, final String key) {
		ResourceBundle b = (ResourceBundle) m_map.get(key); // This bundle is mapped?
		if(b != null) {
			res.add(b);
			return;
		}

		//-- Unknown bundle. Try to locate;
		String rp = (m_bundleKey + key).replace('.', '/') + ".properties"; // Full resource name,
		//		ClassLoader ldr = getClass().getClassLoader();
		ClassLoader ldr = m_loader == null ? getClass().getClassLoader() : m_loader;
		InputStream is = ldr.getResourceAsStream(rp);
		if(is == null) // Cannot locate.
			return;
		try {
			PropertyResourceBundle prb = new PropertyResourceBundle(is);

			//-- Add to map, then add to result
			m_map.put(key, prb);
			res.add(prb);
		} catch(IOException x) {
			System.err.println("Can't load propertyResourceBundle: " + rp);
			x.printStackTrace();
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	private String mkSubKey(final String lang, final String country, final String variant, final String dialect) {
		StringBuilder sb = new StringBuilder(16);
		if(dialect != null) {
			sb.append('_');
			sb.append(dialect);
		}
		if(lang != null) {
			sb.append('_');
			sb.append(lang);
		}
		if(country != null) {
			sb.append('_');
			sb.append(country);
		}
		if(variant != null) {
			sb.append('_');
			sb.append(variant);
		}
		return sb.toString();
	}

	/**
	 * Clears the cache. In use by reloading class mechanism, hence only ever called while developing never in production. Dont use otherwise.
	 */
	public static synchronized void internalClear() {
		m_cachedMap.clear();
	}

	/**
	 * Returns the dotted name of the message bundle as (class package name) '.' (file-name-without-.properties). For
	 * a bundleRef to (to.etc.domui.DomUtil.class, "messages") the name would be "to.etc.domui.messages".
	 * @return
	 */
	@Nonnull
	public String getBundleKey() {
		String key = m_bundleKey;
		if(null == key)
			throw new IllegalStateException("Bundle without key");
		return key;
	}
}
