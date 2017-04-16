package to.etc.domui.themes;

/**
 * The default theme style for pages.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9/2/15.
 */
final public class DefaultThemeVariant implements IThemeVariant {
	static public final DefaultThemeVariant INSTANCE = new DefaultThemeVariant();

	private DefaultThemeVariant() {
	}

	@Override
	public String getVariantName() {
		return "default";
	}
}
