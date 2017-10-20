package to.etc.domui.themes;

import java.util.Objects;

/**
 * Key referring to some theme.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 19-10-17.
 */
public class ThemeKey {
	final private IThemeFactory m_factory;

	final private String m_themeName;

	public ThemeKey(IThemeFactory factory, String themeName) {
		m_factory = factory;
		m_themeName = themeName;
	}

	@Override public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;
		ThemeKey themeKey = (ThemeKey) o;
		return Objects.equals(m_factory, themeKey.m_factory) &&
			Objects.equals(m_themeName, themeKey.m_themeName);
	}

	@Override public int hashCode() {
		return Objects.hash(m_factory, m_themeName);
	}

	public IThemeFactory getFactory() {
		return m_factory;
	}

	public String getThemeName() {
		return m_themeName;
	}
}
