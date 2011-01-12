package to.etc.domui.themes;

import java.util.*;

import to.etc.domui.util.resources.*;

public class SimpleTheme implements ITheme {
	private String m_styleName;

	private ResourceDependencies m_rd;

	private Map<String, Object> m_themeProperties;

	public SimpleTheme(String styleName, Map<String, Object> themeProperties, ResourceDependencies rd) {
		m_styleName = styleName;
		m_themeProperties = themeProperties;
		m_rd = rd;
	}

	@Override
	public String getStylesheet() {
		return "$themes/" + m_styleName + "/style.theme.css";
	}

	@Override
	public ResourceDependencies getDependencies() {
		return m_rd;
	}

	@Override
	public Map<String, Object> getThemeProperties() {
		return m_themeProperties;
	}

	@Override
	public String getIconURL(String icon) {
		return icon;
	}

	@Override
	public String getThemePath(String path) {
		return "$themes/" + m_styleName + "/" + path;
	}
}
