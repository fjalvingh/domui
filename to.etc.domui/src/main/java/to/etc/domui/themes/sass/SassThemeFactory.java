package to.etc.domui.themes.sass;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.server.DomApplication;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.IThemeFactory;
import to.etc.domui.themes.IThemeVariant;
import to.etc.domui.themes.SchemeVariant;
import to.etc.domui.util.resources.ResourceDependencyList;

import java.util.List;

/**
 * Sass based theming engine. The theme is a directory below $themes/scss containing
 * style.scss plus the partials and images that sheet and the components refer to; which
 * directory is decided at application initialization time by constructing this factory
 * with a style name and the variants that style has.
 *
 * <p>The one thing that varies per user session is the variant, a {@link SchemeVariant}: a
 * colour scheme of a nature, named <code>[nature]-[scheme]</code>. Every variant gets the same
 * search path:</p>
 * <pre>
 *	$themes/scss/[style]/[nature]/[scheme]	the scheme's tokens, _scheme.scss
 *	$themes/scss/[style]/[nature]		the nature's palette, which reads them, and its images
 *	$themes/scss/[style]
 *	$themes/scss/all
 * </pre>
 * <p>Because a resource is taken from the first directory on that path that has it, a scheme
 * or a nature overrides whatever it wants to and inherits the rest. The variant's name is also
 * passed to the sheet as the scss variable <code>$themeVariant</code> (with
 * <code>$themeNature</code> and <code>$themeScheme</code>), so a single file can branch on it.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-4-17.
 */
@NonNullByDefault
final public class SassThemeFactory implements IThemeFactory {
	/**
	 * The factory for the theme that DomUI itself ships, with its colour schemes: the light
	 * winter, and Midnight - the first dark one, which a browser that prefers dark gets -
	 * Darcula, Lagoon, Violet and Nord.
	 */
	static public final IThemeFactory INSTANCE = new SassThemeFactory("winter", List.of(
		SchemeVariant.WINTER,
		SchemeVariant.MIDNIGHT,
		SchemeVariant.DARCULA,
		SchemeVariant.LAGOON,
		SchemeVariant.VIOLET,
		SchemeVariant.NORD
	));

	private final String m_styleName;

	private final List<IThemeVariant> m_variants;

	/**
	 * A theme with the variants passed, in the order they are offered; the first light one is the
	 * default.
	 */
	public SassThemeFactory(String styleName, List<IThemeVariant> variants) {
		if(variants.isEmpty())
			throw new IllegalArgumentException("A theme needs at least one variant");
		m_styleName = styleName;
		m_variants = List.copyOf(variants);
	}

	public String getStyleName() {
		return m_styleName;
	}

	@NonNull
	@Override
	public List<IThemeVariant> getVariants() {
		return m_variants;
	}

	@NonNull
	@Override
	public ITheme getTheme(@NonNull DomApplication da, @NonNull IThemeVariant variant) throws Exception {
		String name = variant.getVariantName();
		SchemeVariant scheme = variant instanceof SchemeVariant sv ? sv : SchemeVariant.parse(name);
		if(null == scheme)
			throw new IllegalArgumentException("Theme variant '" + name + "' is not [nature]-[scheme]");
		String base = "$themes/scss/" + m_styleName;
		String nature = base + "/" + scheme.getNature().getName();
		List<String> searchpath = List.of(
			nature + "/" + scheme.getSchemeName(),
			nature,
			base,
			"$themes/scss/all"											// 20130327 jal The "all" folder contains stuff shared for all themes
		);
		return new SassTheme(da, name, new ResourceDependencyList().createDependencies(), searchpath);
	}
}
