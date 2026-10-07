package to.etc.domui.themes;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Whether a colour scheme is light or dark. The nature decides how the theme works its colours
 * out from a scheme's tokens - each nature has its own palette, in
 * <code>$themes/scss/[style]/[nature]</code> - and it is the CSS <code>color-scheme</code> the
 * page is rendered in.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@NonNullByDefault
public enum ThemeNature {
	LIGHT("light"),
	DARK("dark");

	private final String m_name;

	ThemeNature(String name) {
		m_name = name;
	}

	/** The nature's name: its directory below the theme, the first part of a variant name, and the CSS color-scheme. */
	@NonNull
	public String getName() {
		return m_name;
	}

	/** The nature with the name passed, or null when there is none. */
	@Nullable
	static public ThemeNature byName(String name) {
		for(ThemeNature n : values()) {
			if(n.m_name.equals(name))
				return n;
		}
		return null;
	}
}
