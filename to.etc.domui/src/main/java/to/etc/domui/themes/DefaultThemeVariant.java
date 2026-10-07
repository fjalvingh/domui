package to.etc.domui.themes;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.util.Msgs;

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

	@NonNull
	@Override
	public String getLabel() {
		return Msgs.BUNDLE.getString(Msgs.THEME_LIGHT);
	}
}
