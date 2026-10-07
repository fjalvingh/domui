package to.etc.domui.themes.sass;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.server.DomApplication;
import to.etc.domui.themes.DarkSchemeVariant;
import to.etc.domui.themes.DarkThemeVariant;
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
 * <code>dark</code> directory holding its own copies of the two colour files,
 * <code>_palette.scss</code> and <code>_component-colors.scss</code>, plus any image that
 * needs to differ. The variant name is also passed to the sheet as the
 * scss variable <code>$themeVariant</code>, so a single file can branch on it instead.</p>
 *
 * <p>A {@link DarkSchemeVariant} - a variant named <code>scheme-[name]</code> - gets the
 * longer search path that class describes: its scheme, the scheme template, and the dark
 * variant, before the style's own directory.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-4-17.
 */
@NonNullByDefault
final public class SassThemeFactory implements IThemeFactory {
	/** The factory for the theme that DomUI itself ships, with its light and dark variants and its dark colour schemes. */
	static public final IThemeFactory INSTANCE = new SassThemeFactory("winter", List.of(
		DefaultThemeVariant.INSTANCE,
		DarkThemeVariant.INSTANCE,
		DarkSchemeVariant.MIDNIGHT,
		DarkSchemeVariant.LAGOON,
		DarkSchemeVariant.VIOLET,
		DarkSchemeVariant.NORD
	));

	private final String m_styleName;

	private final List<IThemeVariant> m_variants;

	/**
	 * A theme with the default variant only. The style can still have more, which the
	 * application then has to offer itself, in {@link DomApplication#getThemeVariants()}.
	 */
	public SassThemeFactory(String styleName) {
		this(styleName, List.of(DefaultThemeVariant.INSTANCE));
	}

	/**
	 * A theme with the variants passed, which must include the default one.
	 */
	public SassThemeFactory(String styleName, List<IThemeVariant> variants) {
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
		List<String> searchpath = new ArrayList<>();
		if(name.startsWith(DarkSchemeVariant.PREFIX)) {
			searchpath.add("$themes/scss/" + m_styleName + "/" + name);
			searchpath.add("$themes/scss/" + m_styleName + "/scheme");
			searchpath.add("$themes/scss/" + m_styleName + "/" + DarkThemeVariant.INSTANCE.getVariantName());
		} else if(!DefaultThemeVariant.INSTANCE.getVariantName().equals(name))
			searchpath.add("$themes/scss/" + m_styleName + "/" + name);
		searchpath.add("$themes/scss/" + m_styleName);
		searchpath.add("$themes/scss/all");							// 20130327 jal The "all" folder contains stuff shared for all themes

		return new SassTheme(da, name, new ResourceDependencyList().createDependencies(), searchpath);
	}
}
