package to.etc.domui.themes;

/**
 * The rewritten, clean theme style.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9/2/15.
 */
final public class CleanThemeVariant implements IThemeVariant {
	static public final CleanThemeVariant INSTANCE = new CleanThemeVariant();

	private CleanThemeVariant() {
	}

	@Override
	public String getVariantName() {
		return "clean";
	}
}
