package to.etc.server.nls;

import java.util.*;

import to.etc.server.misc.*;

/**
 * A reference to a set of ResourceBundle's for a given bundle key. This
 * maintains a cache of all bundles of this key for different locales.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 18, 2006
 */
public class BundleRef implements NlsMessageProvider {
	private String						m_bundleKey;

	private Map<Locale, ResourceBundle>	m_map	= new HashMap<Locale, ResourceBundle>();

	BundleRef(String key) {
		m_bundleKey = key;
	}

	/**
	 * Returns the bundle for the specified locale from cache, and caches
	 * it if not found.
	 * @param loc
	 * @return
	 * @throws ResourceNotFoundException if ...
	 */
	public synchronized ResourceBundle getBundle(Locale loc) {
		ResourceBundle r = m_map.get(loc);
		if(r == null) {
			r = ResourceBundle.getBundle(m_bundleKey, loc); // Throws exception if not found
			m_map.put(loc, r);
		}
		return r;
	}

	/**
	 * Returns a translation of key in the specified locale (or the one
	 * closest to it). If no translation exists for the message in the
	 * specified bundle then we try the "default" bundle; if it still
	 * does not exist we return a string containing the key with ????.
	 * @param loc
	 * @param key
	 * @throws  ResourceNotFoundException the bundle cannot be located.
	 */
	public String getString(Locale loc, String key) {
		ResourceBundle b = getBundle(loc);
		try {
			return b.getString(key);
		} catch(MissingResourceException x) {
			return "???" + key + "???";
		}
	}

	/**
	 * Returns the translation of the key passed in the <i>current</i> ViewPoint
	 * locale.
	 *
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		return getString(NlsContext.getLocale(), key);
	}

	public String findMessage(Locale loc, String code) {
		ResourceBundle b = getBundle(loc);
		try {
			return b.getString(code);
		} catch(MissingResourceException x) {
			return null;
		}
	}
}
