package to.etc.domui.themes;

/**
 * This helper class is passed to the theme factory, and can be used to augment
 * information in the style.properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 4, 2011
 */
public class ThemeCssUtils {
	static public final CssColor BLACK = new CssColor(0, 0, 0);

	static public final CssColor WHITE = new CssColor(255, 255, 255);

	public CssColor color(String hex) {
		return new CssColor(hex);
	}


}
