package to.etc.domui.themes.sass;

import to.etc.domui.server.*;
import to.etc.domui.themes.*;

import javax.annotation.*;

/**
 * Sass based theming engine.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-4-17.
 */
@DefaultNonNull
public class SassThemeFactory {
	static private final IThemeFactory INSTANCE = new IThemeFactory() {
		@Nonnull
		@Override
		public ITheme getTheme(@Nonnull DomApplication da, @Nonnull String themeName) throws Exception {
			SassThemeFactory stf = new SassThemeFactory(da, themeName);
			return stf.createTheme();
		}
	};

	private final DomApplication m_application;

	private final String m_themeName;

	private SassThemeFactory(DomApplication application, String themeName) {
		m_application = application;
		m_themeName = themeName;
	}

	private ITheme createTheme() {
		//-- Split theme name into css/icons/color
		String[] ar = m_themeName.split("/");
		if(ar.length != 4)
			throw new StyleException("The theme name '" + m_themeName + "' is invalid for "+getClass()+": expecting styleName/icon/color/variant");
		String styleName = ar[0];
		String iconName = ar[1];
		String colorName = ar[2];
		String variant = ar[3];

		//-- Get the scss file for the color.




		return null;
	}
}
