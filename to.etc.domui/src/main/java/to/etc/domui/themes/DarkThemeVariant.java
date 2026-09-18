package to.etc.domui.themes;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The dark variant, which the winter theme that DomUI ships implements in
 * resources/themes/scss/winter/dark. Select it for a session with
 * {@link to.etc.domui.server.IRequestContext#setThemeVariant(IThemeVariant)}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
final public class DarkThemeVariant implements IThemeVariant {
	static public final DarkThemeVariant INSTANCE = new DarkThemeVariant();

	private DarkThemeVariant() {
	}

	@NonNull
	@Override
	public String getVariantName() {
		return "dark";
	}

	@NonNull
	@Override
	public String getColorScheme() {
		return "dark";
	}
}
