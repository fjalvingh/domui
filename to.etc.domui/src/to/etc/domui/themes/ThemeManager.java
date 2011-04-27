package to.etc.domui.themes;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;
import to.etc.template.*;

/**
 * This is used by DomApplication to manage themes. It exists to reduce the code in DomApplication; it
 * cannot be overridden.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 27, 2011
 */
final public class ThemeManager {
	final private DomApplication m_application;

	/** The thing that themes the application. Set only once @ init time. */
	private IThemeFactory m_themeFactory;

	/** The "current theme". This will become part of all themed resource URLs and is interpreted by the theme factory to resolve resources. */
	private String m_currentTheme = "domui";

	static private class ThemeRef {
		final private ITheme m_theme;

		private long m_lastuse;

		final private IIsModified m_rdl;

		public ThemeRef(ITheme theme, IIsModified rdl) {
			m_theme = theme;
			m_rdl = rdl;
		}

		public ITheme getTheme() {
			return m_theme;
		}

		public long getLastuse() {
			return m_lastuse;
		}

		public void setLastuse(long lastuse) {
			m_lastuse = lastuse;
		}

		public IIsModified getDependencies() {
			return m_rdl;
		}
	}

	/** Map of themes by theme name, as implemented by the current engine. */
	private final Map<String, ThemeRef> m_themeMap = new HashMap<String, ThemeRef>();

	public ThemeManager(DomApplication application) {
		m_application = application;
	}

	/**
	 * Sets the current theme string. This string is used as a "parameter" for the theme factory
	 * which will use it to decide on the "real" theme to use.
	 * @param currentTheme	The theme name, valid for the current theme engine. Cannot be null nor the empty string.
	 */
	public synchronized void setCurrentTheme(@Nonnull String currentTheme) {
		if(null == currentTheme)
			throw new IllegalArgumentException("This cannot be null");
		m_currentTheme = currentTheme;
	}

	/**
	 * Gets the current theme string.  This will become part of all themed resource URLs
	 * and is interpreted by the theme factory to resolve resources.
	 * @return
	 */
	@Nonnull
	public synchronized String getCurrentTheme() {
		return m_currentTheme;
	}

	/**
	 * Get the current theme factory.
	 * @return
	 */
	@Nonnull
	public synchronized IThemeFactory getThemeFactory() {
		if(m_themeFactory == null)
			throw new IllegalStateException("Theme factory cannot be null");
		return m_themeFactory;
	}

	/**
	 * Set the factory for handling the theme.
	 * @param themer
	 */
	public synchronized void setThemeFactory(@Nonnull IThemeFactory themer) {
		if(themer == null)
			throw new IllegalStateException("Theme factory cannot be null");
		m_themeFactory = themer;
		m_themeMap.clear();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Getting a theme instance.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Get the theme store representing the specified theme name. This is the name as obtained
	 * from the resource name which is the part between $THEME/ and the actual filename. This
	 * code is fast once the theme is loaded after the 1st call.
	 *
	 * @param rdl
	 * @return
	 * @throws Exception
	 */
	public ITheme getTheme(String themeName, @Nullable IResourceDependencyList rdl) throws Exception {
		synchronized(this) {
			ThemeRef tr = m_themeMap.get(themeName);
			if(tr != null) {
				//-- Developer mode: is the theme still valid?
				if(tr.getDependencies() == null || !tr.getDependencies().isModified()) {
					if(rdl != null && tr.getDependencies() != null)
						rdl.add(tr.getDependencies());
					tr.setLastuse(System.currentTimeMillis());
					return tr.getTheme();
				}
			}

			//-- No such cached theme yet, or the theme has changed. (Re)load it.
			ITheme theme = getThemeFactory().getTheme(m_application, themeName);
			if(null == theme)
				throw new IllegalStateException("Theme factory returned null!?");
			ResourceDependencies deps = null;
			if(m_application.inDevelopmentMode()) {
				ThemeModifyableResource tmr = new ThemeModifyableResource(theme.getDependencies(), 3000);
				deps = new ResourceDependencies(new IIsModified[]{tmr});
			}
			tr = new ThemeRef(theme, deps);
			if(rdl != null && deps != null)
				rdl.add(deps);
			m_themeMap.put(themeName, tr);
			return theme;
		}
	}


	public String getThemeReplacedString(@Nonnull IResourceDependencyList rdl, String rurl) throws Exception {
		return getThemeReplacedString(rdl, rurl, null);
	}

	/**
	 * EXPENSIVE CALL - ONLY USE TO CREATE CACHED RESOURCES
	 *
	 * This loads a theme resource as an utf-8 encoded template, then does expansion using the
	 * current theme's variable map. This map is either a "style.properties" file
	 * inside the theme's folder, or can be configured dynamically using a IThemeMapFactory.
	 *
	 * The result is returned as a string.
	 *
	 * @param rdl
	 * @param key
	 * @return
	 */
	public String getThemeReplacedString(@Nonnull IResourceDependencyList rdl, @Nonnull String rurl, @Nullable BrowserVersion bv) throws Exception {
		//		long ts = System.nanoTime();
		IResourceRef ires = m_application.getResource(rurl, rdl); // Get the template source file
		//		if(ires == null)
		//			throw new ThingyNotFoundException("The theme-replaced file " + rurl + " cannot be found");

		//-- Get the variable map to use.
		Map<String, Object> themeMap = getThemeMap(rdl);
		themeMap = new HashMap<String, Object>(themeMap); // Create a modifyable duplicate
		if(bv != null) {
			themeMap.put("browser", bv);
		}
		themeMap.put("util", new ThemeCssUtils());

		m_application.augmentThemeMap(themeMap); // Provide a hook to let user code add stuff to the theme map

		//-- 2. Get a reader.
		InputStream is = ires.getInputStream();
		if(is == null) {
			System.out.println(">>>> RESOURCE ERROR: " + rurl + ", ref=" + ires);
			throw new ThingyNotFoundException("Unexpected: cannot get input stream for IResourceRef rurl=" + rurl + ", ref=" + ires);
		}
		try {
			Reader r = new InputStreamReader(is, "utf-8");
			StringBuilder sb = new StringBuilder(65536);

			JSTemplateCompiler tc = new JSTemplateCompiler();
			tc.executeMap(sb, r, rurl, themeMap);

			//			ts = System.nanoTime() - ts;
			//			System.out.println("theme-replace: " + rurl + " took " + StringTool.strNanoTime(ts));
			return sb.toString();
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Return the current theme map (a readonly map), cached from the last
	 * time. It will refresh automatically when the resource dependencies
	 * for the theme are updated.
	 *
	 * @param rdl
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getThemeMap(IResourceDependencyList rdlin) throws Exception {
		ITheme ts = getTheme(rdlin);
		Map<String, Object> tmap = ts.getThemeProperties();
		return tmap;
	}

	/**
	 * This checks to see if the RURL passed is a theme-relative URL. These URLs start
	 * with THEME/. If not the RURL is returned as-is; otherwise the URL is translated
	 * to a path containing the current theme string:
	 * <pre>
	 * 	$THEME/[currentThemeString]/[name]
	 * </pre>
	 * where [name] is the rest of the path string after THEME/ has been removed from it.
	 * @param path
	 * @return
	 */
	@Nullable
	public String getThemedResourceRURL(String path) {
		if(null == path)
			return null;
		if(path.startsWith("THEME/"))
			path = path.substring(6); // Strip THEME/
		else if(path.startsWith("ICON/"))
			throw new IllegalStateException("Bad ROOT: ICON/. Use THEME/ instead.");
		else
			return path; // Not theme-relative, so return as-is.

		return ThemeResourceFactory.PREFIX + getCurrentTheme() + "/" + path;
	}


}
