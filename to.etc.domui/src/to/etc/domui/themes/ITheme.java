package to.etc.domui.themes;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.util.resources.*;

/**
 * A theme.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 12, 2011
 */
public interface ITheme {
	/**
	 * The dependencies for this theme instance. This will be used by the engine to check
	 * if this instance needs to be reloaded because it's source files have changed in
	 * development mode.
	 * @return
	 */
	@Nonnull
	ResourceDependencies getDependencies();

	/**
	 * Returns the stylesheet RURL to include in every page. This must return an application-relative URL,
	 * i.e. it must <b>not</b> include the webapp's context and must not start with a /.
	 * @return
	 */
	String getStylesheet();

	/**
	 * Get a resource from the theme's inheritance path.
	 * @param path
	 * @return
	 */
	String getThemePath(String path) throws Exception;

	/**
	 * Return the read-only properties for a theme.
	 * @return
	 */
	@Nonnull
	Map<String, Object> getThemeProperties();

	//	/**
	//	 * Return the primary css stylesheet template. This gets expanded for every browser
	//	 * type separately.
	//	 * @return
	//	 */
	//	JSTemplate getStylesheetTemplate();

	/**
	 * Find the specified icon in the theme, and return the proper RURL for it.
	 * @param icon
	 * @return
	 */
	@Nullable
	String getIconURL(@Nonnull String icon) throws Exception;
}

