package to.etc.domui.themes.sass;

import to.etc.domui.server.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.js.*;
import to.etc.domui.util.resources.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * Sass based theming engine.
 *
 * This basic theme engine does not use fragments. A sass theme resource has a path that is constructed
 * as follows:
 * <pre>
 * $themes/styleName/iconname/colorname/variant/xxxx
 * </pre>
 * <p>The theme itself is mostly statically defined by using fragments that all must be included
 * using style.scss, the theme's main stylesheet. This includes all the fragments and refers
 * all resources which, by definition, also use that same path as the basis.</p>
 * <p>When the theme needs a file it searches the <em>theme search path</em>, which is constructed
 * in the following way:</p>
 * <ul>
 *	<li>If a color different from "default" is present: add the path $themes/scss/[styleName]/color-[colorName] to
 *		the search path</li>
 *	<li>If an icon set different from "default" is specified: add the path $themes/scss/[stylename]/icons-[iconname]
 * 		to the search path</li>
 * </ul>
 * <p>The theme.scss refers to _color.scss to include color variables; by putting this file in the different
 * color-xxx subdirectories the colors can be changed, because those directories are earlier in the path. The
 * same trick works for icons.
 * </p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-4-17.
 */
@DefaultNonNull
final public class SassThemeFactory {
	static public final IThemeFactory INSTANCE = new IThemeFactory() {
		@Nonnull
		@Override
		public ITheme getTheme(@Nonnull DomApplication da, @Nonnull String themeName) throws Exception {
			SassThemeFactory stf = new SassThemeFactory(da, themeName);
			return stf.createTheme();
		}

		@Nonnull @Override public String getFactoryName() {
			return "scss";
		}

		@Nonnull @Override public String getDefaultThemeName() {
			return getFactoryName() + "-winter-default-default";
		}
	};

	private final DomApplication m_application;

	private final String m_themeName;

	private SassThemeFactory(DomApplication application, String themeName) {
		m_application = application;
		m_themeName = themeName;
	}

	private ITheme createTheme() throws Exception {
		//-- Split theme name into css/icons/color
		String[] ar = m_themeName.split("-");
		if(ar.length != 4 && ar.length != 5)
			throw new StyleException("The theme name '" + m_themeName + "' is invalid for "+getClass()+": expecting factory-styleName-icon-color-variant");
		String styleName = ar[1];
		String iconName = ar[2];
		String colorName = ar[3];
		String variant = ar.length == 4 ? DefaultThemeVariant.INSTANCE.getVariantName() : ar[4];

		//-- Check that the required files exist; this will throw an exception if not
		List<String> searchpath = new ArrayList<>();
		if(! "default".equals(colorName))
			searchpath.add("$themes/scss/" + styleName + "/" + colorName + "-color");
		if(!"default".equals(iconName))
			searchpath.add("$themes/scss/" + styleName + "/" + iconName + "-icons");
		searchpath.add("$themes/scss/" + styleName);
		searchpath.add("$themes/scss/all");							// 20130327 jal The "all" folder contains stuff shared for all themes

		IScriptScope iss = new IScriptScope() {
			@Nullable @Override public <T> T getValue(@Nonnull Class<T> valueClass, @Nonnull String name) {
				return null;
			}

			@Override public <T> void put(@Nonnull String name, @Nullable T instance) {

			}

			@Nonnull @Override public <T> List<T> getProperties(@Nonnull Class<T> filterClass) {
				return Collections.EMPTY_LIST;
			}

			@Nonnull @Override public IScriptScope addObjectProperty(@Nonnull String name) {
				return this;
			}

			@Nullable @Override public <T> T eval(@Nonnull Class<T> targetType, @Nonnull Reader r, @Nonnull String sourceFileNameIndicator) throws Exception {
				return null;
			}

			@Nullable @Override public <T> T eval(@Nonnull Class<T> targetType, @Nonnull String expression, @Nonnull String sourceFileNameIndicator) throws Exception {
				return null;
			}

			@Nonnull @Override public IScriptScope newScope() {
				return this;
			}

			@Nullable @Override public <T> T getAdapter(@Nonnull Class<T> clz) {
				return null;
			}
		};

		return new SassTheme(m_application, m_themeName, styleName, iss, new ResourceDependencyList().createDependencies(), searchpath);
	}
}
