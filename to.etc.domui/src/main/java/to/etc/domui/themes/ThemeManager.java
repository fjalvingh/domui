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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.util.resources.IIsModified;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.ResourceDependencies;
import to.etc.util.WrappedException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is used by DomApplication to manage themes. It exists to reduce the code in DomApplication; it
 * cannot be overridden.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 27, 2011
 */
final public class ThemeManager {
	static private final long OLD_THEME_TIME = 5L * 60 * 1000;

	final private DomApplication m_application;

	/** Map of themes by variant name; the theme factory itself is fixed for the application. */
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
			m_lastuse = System.currentTimeMillis();
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
	 * Cached get of the ITheme for a variant. This code is fast once the theme is loaded
	 * after the 1st call.
	 * FIXME Get rid of rdl parameter
	 */
	@NonNull
	public ITheme getTheme(@NonNull IThemeVariant variant, @Nullable IResourceDependencyList rdl) {
		String key = variant.getVariantName();

		synchronized(this) {
			if(m_themeReapCount++ > 1000) {
				m_themeReapCount = 0;
				checkReapThemes();
			}

			ThemeRef tr = m_themeMap.get(key);
			//-- Developer mode: is the theme still valid?
			if(tr != null && (tr.getDependencies() == null || !tr.getDependencies().isModified())) {
				if(rdl != null && tr.getDependencies() != null)
					rdl.add(tr.getDependencies());
				tr.setLastuse(System.currentTimeMillis());
				return tr.getTheme();
			}

			//-- No such cached theme yet, or the theme has changed. (Re)load it.
			ITheme theme;
			try {
				theme = m_application.getThemeFactory().getTheme(m_application, variant);
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
	 * Get the ITheme for the variant name taken from a themed resource URL.
	 */
	@NonNull
	public ITheme getTheme(@NonNull String variantName, @Nullable IResourceDependencyList rdl) {
		return getTheme(IThemeVariant.of(variantName), rdl);
	}

	/**
	 * Check to see if there are "old" themes (not used for > 5 minutes)
	 * that we can reap. We will always retain the most recently used theme.
	 */
	private synchronized void checkReapThemes() {
		long ts = System.currentTimeMillis();
		if(ts < m_themeNextReapTS)
			return;
		m_themeNextReapTS = ts + OLD_THEME_TIME;

		//-- Find the most recently used theme; that one is always retained.
		ThemeRef newest = null;
		for(ThemeRef tr : m_themeMap.values()) {
			if(newest == null || tr.getLastuse() > newest.getLastuse())
				newest = tr;
		}

		long abstime = ts - OLD_THEME_TIME;
		for(Iterator<Map.Entry<String, ThemeRef>> it = m_themeMap.entrySet().iterator(); it.hasNext();) {
			ThemeRef tr = it.next().getValue();
			if(tr != newest && tr.getLastuse() < abstime)
				it.remove();
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
	@NonNull
	public String getThemedResourceRURL(@NonNull IRequestContext context, @NonNull String path) {
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
	@NonNull
	public String getThemedResourceRURL(@NonNull ITheme theme, @NonNull String path) {
		if(path.startsWith("THEME/")) {
			path = path.substring(6); 							// Strip THEME/
		} else if(path.startsWith("ICON/")) {
			throw new IllegalStateException("Bad ROOT: ICON/. Use THEME/ instead.");
		} else
			return path;										// Not theme-relative, so return as-is.
		try {
			String newicon = theme.translateResourceName(path);
			return ThemeResourceFactory.PREFIX + theme.getVariantName() + "/" + newicon;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

}
