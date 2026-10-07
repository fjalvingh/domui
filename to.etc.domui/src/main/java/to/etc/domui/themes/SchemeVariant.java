package to.etc.domui.themes;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.themes.sass.SassThemeFactory;

/**
 * A theme variant: a colour scheme of a {@link ThemeNature}. Its name is
 * <code>[nature]-[scheme]</code>, for instance <code>light-winter</code> or
 * <code>dark-nord</code>, and {@link SassThemeFactory} gives every variant the same search path:
 * <pre>
 *	$themes/scss/[style]/[nature]/[scheme]	the scheme: _scheme.scss, and any _scheme-exceptions.scss
 *	$themes/scss/[style]/[nature]		the nature: _palette.scss, which reads the scheme, and its images
 *	$themes/scss/[style]
 *	$themes/scss/all
 * </pre>
 * <p>A scheme is the one file <code>_scheme.scss</code>: a few dozen colours, the tokens its
 * nature's palette works every other colour out from. The winter theme ships the schemes below.
 * An application adds a scheme of its own by putting a
 * <code>themes/scss/winter/[nature]/[scheme]/_scheme.scss</code> in its webapp, a copy of one of
 * DomUI's with the colours changed, and offering it in
 * {@link to.etc.domui.server.DomApplication#getThemeVariants()}.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@NonNullByDefault
final public class SchemeVariant implements IThemeVariant {
	/** DomUI's light theme. */
	static public final SchemeVariant WINTER = new SchemeVariant(ThemeNature.LIGHT, "winter", "Winter");

	/** Deep indigo grounds, electric blue structure, neon accents (after Tokyo Night). */
	static public final SchemeVariant MIDNIGHT = new SchemeVariant(ThemeNature.DARK, "midnight", "Midnight");

	/** Neutral greys with muted, desaturated colours (after IntelliJ's Darcula). */
	static public final SchemeVariant DARCULA = new SchemeVariant(ThemeNature.DARK, "darcula", "Darcula");

	/** Deep sea-green grounds with vivid teal structure: the complement of the orange accent. */
	static public final SchemeVariant LAGOON = new SchemeVariant(ThemeNature.DARK, "lagoon", "Lagoon");

	/** Aubergine grounds, violet structure, pink and cyan accents (after Dracula). */
	static public final SchemeVariant VIOLET = new SchemeVariant(ThemeNature.DARK, "violet", "Violet");

	/** Arctic blue-grey grounds, frost blue structure, soft aurora colours: the calm one (after Nord). */
	static public final SchemeVariant NORD = new SchemeVariant(ThemeNature.DARK, "nord", "Nord");

	private final ThemeNature m_nature;

	private final String m_schemeName;

	private final String m_label;

	/**
	 * Define the scheme in the directory <code>[nature]/[schemeName]</code>, which a user is
	 * offered as the label passed.
	 */
	public SchemeVariant(ThemeNature nature, String schemeName, String label) {
		if(!isSchemeName(schemeName))
			throw new IllegalArgumentException("Bad scheme name: '" + schemeName + "'");
		m_nature = nature;
		m_schemeName = schemeName;
		m_label = label;
	}

	/**
	 * The variant a name stands for, or null when the name is not <code>[nature]-[scheme]</code>.
	 * Whether that scheme exists is not checked: see
	 * {@link to.etc.domui.server.DomApplication#findThemeVariant(String)} for that. The variant
	 * returned has its scheme name as label.
	 */
	@Nullable
	static public SchemeVariant parse(String variantName) {
		int ix = variantName.indexOf('-');
		if(ix < 0)
			return null;
		ThemeNature nature = ThemeNature.byName(variantName.substring(0, ix));
		String scheme = variantName.substring(ix + 1);
		if(null == nature || !isSchemeName(scheme))
			return null;
		return new SchemeVariant(nature, scheme, scheme);
	}

	static private boolean isSchemeName(String name) {
		return !name.isEmpty() && name.chars().allMatch(c -> (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '_');
	}

	public ThemeNature getNature() {
		return m_nature;
	}

	/** The scheme's name: its directory below the nature's, and the variant's name without the nature. */
	public String getSchemeName() {
		return m_schemeName;
	}

	@NonNull
	@Override
	public String getVariantName() {
		return m_nature.getName() + "-" + m_schemeName;
	}

	@NonNull
	@Override
	public String getLabel() {
		return m_label;
	}

	@NonNull
	@Override
	public String getColorScheme() {
		return m_nature.getName();
	}

	@Override
	public boolean equals(@Nullable Object o) {
		return o instanceof SchemeVariant sv && sv.getVariantName().equals(getVariantName());
	}

	@Override
	public int hashCode() {
		return getVariantName().hashCode();
	}

	@Override
	public String toString() {
		return getVariantName();
	}
}
