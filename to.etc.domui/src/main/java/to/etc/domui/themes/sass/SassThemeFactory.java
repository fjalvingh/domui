package to.etc.domui.themes.sass;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.server.DomApplication;
import to.etc.domui.themes.DefaultThemeVariant;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.IThemeFactory;
import to.etc.domui.themes.IThemeVariant;
import to.etc.domui.util.resources.ResourceDependencyList;

import java.util.ArrayList;
import java.util.List;

/**
 * Sass based theming engine. The theme is a directory below $themes/scss containing
 * style.scss plus the partials and images that sheet and the components refer to; which
 * directory is decided at application initialization time by constructing this factory
 * with a style name.
 *
 * <p>The one thing that varies per user session is the {@link IThemeVariant}. A variant
 * adds a directory <i>before</i> the style's own on the theme search path:</p>
 * <pre>
 *	$themes/scss/[style]/[variant]		only when the variant is not "default"
 *	$themes/scss/[style]
 *	$themes/scss/all
 * </pre>
 * <p>Because a resource is taken from the first directory on that path that has it, a
 * variant overrides whatever it wants to and inherits the rest. A dark theme is a
 * <code>dark</code> directory holding its own <code>_color.scss</code>, plus any image
 * that needs to differ; <code>style.scss</code> keeps its plain
 * <code>&#64;import 'color'</code>. The variant name is also passed to the sheet as the
 * scss variable <code>$themeVariant</code>, so a single file can branch on it instead.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-4-17.
 */
@NonNullByDefault
final public class SassThemeFactory implements IThemeFactory {
	/** The factory for the theme that DomUI itself ships. */
	static public final IThemeFactory INSTANCE = new SassThemeFactory("winter");

	private final String m_styleName;

	public SassThemeFactory(String styleName) {
		m_styleName = styleName;
	}

	public String getStyleName() {
		return m_styleName;
	}

	@NonNull
	@Override
	public ITheme getTheme(@NonNull DomApplication da, @NonNull IThemeVariant variant) throws Exception {
		List<String> searchpath = new ArrayList<>();
		if(!DefaultThemeVariant.INSTANCE.getVariantName().equals(variant.getVariantName()))
			searchpath.add("$themes/scss/" + m_styleName + "/" + variant.getVariantName());
		searchpath.add("$themes/scss/" + m_styleName);
		searchpath.add("$themes/scss/all");							// 20130327 jal The "all" folder contains stuff shared for all themes

		return new SassTheme(da, variant.getVariantName(), new ResourceDependencyList().createDependencies(), searchpath);
	}
}
