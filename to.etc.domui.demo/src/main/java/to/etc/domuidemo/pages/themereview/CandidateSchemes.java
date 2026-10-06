package to.etc.domuidemo.pages.themereview;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.server.DomApplication;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.IThemeFactory;
import to.etc.domui.themes.IThemeVariant;
import to.etc.domui.themes.sass.SassTheme;
import to.etc.domui.themes.sass.SassThemeFactory;
import to.etc.domui.util.resources.ResourceDependencyList;

import java.util.List;

/**
 * Candidate colour schemes for the dark variant, so that they can be compared on the
 * {@link ThemeReviewPage} before one of them becomes the real one.
 *
 * <p>A candidate is a theme variant named <code>scheme-[name]</code>. Its colours are one file,
 * <code>themes/scss/winter/scheme-[name]/_scheme.scss</code> in the demo's webapp, which the
 * template in <code>themes/scss/winter/scheme/</code> reads: that template is the dark variant's
 * two colour files with every colour taken from the scheme. Behind those two directories the
 * candidate has the dark variant's own directory, for its images, and then the theme itself:</p>
 * <pre>
 *	$themes/scss/winter/scheme-[name]	the scheme
 *	$themes/scss/winter/scheme		the template: _palette.scss and _component-colors.scss
 *	$themes/scss/winter/dark		the dark variant's images and _variant-custominit.scss
 *	$themes/scss/winter
 *	$themes/scss/all
 * </pre>
 * <p>Every other variant is left to the stock {@link SassThemeFactory}.</p>
 */
@NonNullByDefault
final public class CandidateSchemes implements IThemeFactory {
	static public final String PREFIX = "scheme-";

	/** The candidates, as the names of their directories after the prefix, and what to call them. */
	static public final List<String[]> SCHEMES = List.of(
		new String[]{"midnight", "Midnight"},
		new String[]{"lagoon", "Lagoon"},
		new String[]{"violet", "Violet"},
		new String[]{"nord", "Nord"}
	);

	static public final IThemeFactory INSTANCE = new CandidateSchemes();

	private CandidateSchemes() {
	}

	@NonNull
	@Override
	public ITheme getTheme(@NonNull DomApplication da, @NonNull IThemeVariant variant) throws Exception {
		String name = variant.getVariantName();
		if(!name.startsWith(PREFIX))
			return SassThemeFactory.INSTANCE.getTheme(da, variant);

		String base = "$themes/scss/winter";
		List<String> searchpath = List.of(
			base + "/" + name,
			base + "/scheme",
			base + "/dark",
			base,
			"$themes/scss/all"
		);
		return new SassTheme(da, name, new ResourceDependencyList().createDependencies(), searchpath);
	}

	static public IThemeVariant variant(String scheme) {
		return IThemeVariant.of(PREFIX + scheme);
	}
}
