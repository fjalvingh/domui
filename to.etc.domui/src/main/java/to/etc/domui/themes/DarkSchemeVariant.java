package to.etc.domui.themes;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.themes.sass.SassThemeFactory;

/**
 * A dark colour scheme: the dark variant with every one of its colours taken from a single
 * file of a few dozen colours, the scheme. The variant is named <code>scheme-[name]</code>,
 * and {@link SassThemeFactory} gives it this search path:
 * <pre>
 *	$themes/scss/[style]/scheme-[name]	the scheme: _scheme.scss
 *	$themes/scss/[style]/scheme		the template: _palette.scss and _component-colors.scss
 *	$themes/scss/[style]/dark		the dark variant's images and _variant-custominit.scss
 *	$themes/scss/[style]
 *	$themes/scss/all
 * </pre>
 * <p>The template is the dark variant's two colour files with every colour read from the
 * scheme, so a scheme is that one file. The winter theme ships the template and the four
 * schemes below. An application adds a scheme of its own by putting a
 * <code>themes/scss/winter/scheme-[name]/_scheme.scss</code> in its webapp, a copy of one
 * of DomUI's with the colours changed, and offering it in
 * {@link to.etc.domui.server.DomApplication#getThemeVariants()}.</p>
 */
@NonNullByDefault
final public class DarkSchemeVariant implements IThemeVariant {
	/** What the name of a scheme's variant, and of its directory, starts with. */
	static public final String PREFIX = "scheme-";

	/** Deep indigo grounds, electric blue structure, neon accents (after Tokyo Night). */
	static public final DarkSchemeVariant MIDNIGHT = new DarkSchemeVariant("midnight", "Midnight");

	/** Deep sea-green grounds with vivid teal structure: the complement of the orange accent. */
	static public final DarkSchemeVariant LAGOON = new DarkSchemeVariant("lagoon", "Lagoon");

	/** Aubergine grounds, violet structure, pink and cyan accents (after Dracula). */
	static public final DarkSchemeVariant VIOLET = new DarkSchemeVariant("violet", "Violet");

	/** Arctic blue-grey grounds, frost blue structure, soft aurora colours: the calm one (after Nord). */
	static public final DarkSchemeVariant NORD = new DarkSchemeVariant("nord", "Nord");

	private final String m_schemeName;

	private final String m_label;

	/**
	 * Define the scheme in the directory <code>scheme-[schemeName]</code>, which a user is
	 * offered as the label passed.
	 */
	public DarkSchemeVariant(String schemeName, String label) {
		if(schemeName.isEmpty() || schemeName.indexOf('/') != -1)
			throw new IllegalArgumentException("Bad scheme name: '" + schemeName + "'");
		m_schemeName = schemeName;
		m_label = label;
	}

	/** The scheme's name: the variant's name without the prefix. */
	public String getSchemeName() {
		return m_schemeName;
	}

	@NonNull
	@Override
	public String getVariantName() {
		return PREFIX + m_schemeName;
	}

	@NonNull
	@Override
	public String getLabel() {
		return m_label;
	}

	@NonNull
	@Override
	public String getColorScheme() {
		return "dark";
	}

	@Override
	public String toString() {
		return getVariantName();
	}
}
