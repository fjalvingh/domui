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
package to.etc.domui.themes;

import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.js.IScriptScope;
import to.etc.domui.util.js.RhinoTemplateCompiler;
import to.etc.domui.util.resources.IIsModified;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.domui.util.resources.ResourceDependencies;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is used by DomApplication to manage themes. It exists to reduce the code in DomApplication; it
 * cannot be overridden.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 27, 2011
 */
final public class ThemeManager {
	static private final long OLD_THEME_TIME = 5 * 60 * 1000;

	final private DomApplication m_application;

	/** Map of themes by theme name, as implemented by the current engine. */
	private final Map<String, ThemeRef> m_themeMap = new HashMap<>();

	private int m_themeReapCount;

	private long m_themeNextReapTS;

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

	public ThemeManager(DomApplication application) {
		m_application = application;
	}

	/**
	 * Cached get of a factory/theme ITheme instance.
	 * FIXME Get rid of rdl parameter
	 *
	 * Get the theme store representing the specified theme name. This is the name as obtained
	 * from the resource name which is the part between $THEME/ and the actual filename. This
	 * code is fast once the theme is loaded after the 1st call.
	 */
	@Nonnull
	public ITheme getTheme(@Nonnull String themeName, @Nonnull IThemeVariant variant, @Nullable IResourceDependencyList rdl) {
		IThemeFactory factory = DomApplication.getFactoryFromThemeName(themeName);
		return getTheme(factory.appendThemeVariant(themeName, variant), rdl);
	}

	public ITheme getTheme(String key, @Nullable IResourceDependencyList rdl) {
		IThemeFactory factory = DomApplication.getFactoryFromThemeName(key);

		synchronized(this) {
			if(m_themeReapCount++ > 1000) {
				m_themeReapCount = 0;
				checkReapThemes();
			}

			ThemeRef tr = m_themeMap.get(key);
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
			ITheme theme;
			try {
				theme = factory.getTheme(m_application, key);
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
			if(null == theme)
				throw new IllegalStateException("Theme factory returned null!?");
			ResourceDependencies deps = null;
			if(m_application.inDevelopmentMode()) {
				ThemeModifiableResource tmr = new ThemeModifiableResource(theme.getDependencies(), 3000);
				deps = new ResourceDependencies(new IIsModified[]{tmr});
			}
			tr = new ThemeRef(theme, deps);
			if(rdl != null && deps != null)
				rdl.add(deps);
			m_themeMap.put(key, tr);
			return theme;
		}
	}

	/**
	 * Check to see if there are "old" themes (not used for > 5 minutes)
	 * that we can reap. We will always retain the most recently used theme.
	 */
	private synchronized void checkReapThemes() {
		long ts = System.currentTimeMillis();
		if(ts < m_themeNextReapTS)
			return;

		//-- Get a list of all themes and sort in ascending time order.
		List<ThemeRef> list = new ArrayList<>(m_themeMap.values());
		list.sort((a, b) -> {
			long d = a.getLastuse() - b.getLastuse();
			return d == 0 ? 0 : d > 0 ? 1 : -1;
		});

		long abstime = ts - OLD_THEME_TIME;
		for(int i = list.size()-1; --i >= 0;) {
			ThemeRef tr = list.get(i);
			if(tr.getLastuse() < abstime)
				list.remove(i);
		}
		m_themeNextReapTS = ts + OLD_THEME_TIME;
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
	 */
	public String getThemeReplacedString(@Nonnull IResourceDependencyList rdl, @Nonnull String resourceURL, @Nullable BrowserVersion bv) throws Exception {
		long ts = System.nanoTime();
		IResourceRef ires = m_application.getResource(resourceURL, rdl);			// Get the template source file
		if(!ires.exists()) {
			System.out.println(">>>> RESOURCE ERROR: " + resourceURL + ", ref=" + ires);
			throw new ThingyNotFoundException("Unexpected: cannot get input stream for IResourceRef rurl=" + resourceURL + ", ref=" + ires);
		}

		String[] spl = ThemeResourceFactory.splitThemeResourceURL(resourceURL);
		ITheme theme = getTheme(spl[0], null);					// Dependencies already added by get-resource call.
		IScriptScope ss = theme.getPropertyScope();
		ss = ss.newScope();

		if(bv != null) {
			ss.put("browser", bv);
		}
		m_application.augmentThemeMap(ss); // Provide a hook to let user code add stuff to the theme map

		//-- 2. Get a reader.
		InputStream is = ires.getInputStream();
		if(is == null) {
			System.out.println(">>>> RESOURCE ERROR: " + resourceURL + ", ref=" + ires);
			throw new ThingyNotFoundException("Unexpected: cannot get input stream for IResourceRef rurl=" + resourceURL + ", ref=" + ires);
		}
		try {
			Reader r = new InputStreamReader(is, "utf-8");
			StringBuilder sb = new StringBuilder(65536);

			RhinoTemplateCompiler rtc = new RhinoTemplateCompiler();
			rtc.execute(sb, r, resourceURL, ss);
			ts = System.nanoTime() - ts;
			if(bv != null)
				System.out.println("theme-replace: " + resourceURL + " for " + bv.getBrowserName() + ":" + bv.getMajorVersion() + " took " + StringTool.strNanoTime(ts));
			else
				System.out.println("theme-replace: " + resourceURL + " for all browsers took " + StringTool.strNanoTime(ts));
			return sb.toString();
		} finally {
			try {
				is.close();
			} catch(Exception x) {
				// Ignore close exception.
			}
		}
	}

	/**
	 * FIXME Variant kludge
	 *
	 * Return the current theme map (a readonly map), cached from the last
	 * time. It will refresh automatically when the resource dependencies
	 * for the theme are updated.
	 */
	@Deprecated
	public IScriptScope getThemeMap(String themeName, @Nonnull IThemeVariant variant, IResourceDependencyList rdlin) throws Exception {
		ITheme ts = getTheme(themeName, variant, rdlin);
		return ts.getPropertyScope();
	}

	/**
	 * This checks to see if the RURL passed is a theme-relative URL. These URLs start
	 * with THEME/. If not the RURL is returned as-is; otherwise the URL is translated
	 * to a path containing the current theme string:
	 * <pre>
	 * 	$THEME/[currentThemeString]/[name]
	 * </pre>
	 * where [name] is the rest of the path string after THEME/ has been removed from it.
	 */
	@Nonnull
	public String getThemedResourceRURL(@Nonnull IRequestContext context, @Nonnull String path) {
		try {
			ITheme theme = context.getCurrentTheme();
			return getThemedResourceRURL(theme, path);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	/**
	 * This checks to see if the RURL passed is a theme-relative URL. These URLs start
	 * with THEME/. If not the RURL is returned as-is; otherwise the URL is translated
	 * to a path containing the current theme string:
	 * <pre>
	 * 	$THEME/[currentThemeString]/[name]
	 * </pre>
	 * where [name] is the rest of the path string after THEME/ has been removed from it.
	 */
	@Nonnull
	public String getThemedResourceRURL(@Nonnull ITheme theme, @Nonnull String path) {
		if(path.startsWith("THEME/")) {
			path = path.substring(6); 							// Strip THEME/
		} else if(path.startsWith("ICON/")) {
			throw new IllegalStateException("Bad ROOT: ICON/. Use THEME/ instead.");
		} else
			return path;										// Not theme-relative, so return as-is.
		try {
			String newicon = theme.translateResourceName(path);
			return ThemeResourceFactory.PREFIX + theme.getThemeName() + "/" + newicon;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

}
