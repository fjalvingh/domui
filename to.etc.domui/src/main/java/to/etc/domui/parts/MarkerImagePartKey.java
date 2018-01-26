/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.parts;

import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IExtendedParameterInfo;
import to.etc.domui.themes.ITheme;
import to.etc.domui.util.DomUtil;
import to.etc.util.StringTool;

/**
 * Key for cache. Used in {@link MarkerImagePart}
 *
 *
 * @author <a href="mailto:btadic@execom.eu">Bojan Tadic</a>
 * Created on Nov 3, 2011
 */
final public class MarkerImagePartKey {
	static final String PARAM_ICON = "icon";

	static final String PARAM_CAPTION = "caption";

	static final String PARAM_COLOR = "color";

	static final String PARAM_FONT = "font";

	static final String PARAM_FONTSIZE = "fontsize";

	static final String PARAM_SPEC = "fontspec";

	private static final String DEFAULT_ICON = "THEME/icon-search.png";

	private String m_icon;

	private String m_caption;

	private String m_color;

	private String m_font;

	private int m_fontSize;

	public enum FontSpec {
		NORM, BOLD, ITALICS, BOLD_ITALICS
	}

	private FontSpec m_fontSpec;

	static public MarkerImagePartKey decode(DomApplication da, IExtendedParameterInfo info) {
		MarkerImagePartKey k = new MarkerImagePartKey();

		String icon = info.getParameter(PARAM_ICON);
		ITheme theme = da.internalGetThemeManager().getTheme(info.getThemeName(), null);
		String url = da.internalGetThemeManager().getThemedResourceRURL(theme, icon == null || DomUtil.isBlank(icon) ? DEFAULT_ICON : icon.trim());
		k.setIcon(url);

		k.setCaption(info.getParameter(PARAM_CAPTION));
		k.setColor(info.getParameter(PARAM_COLOR));
		k.setFont(info.getParameter(PARAM_FONT));
		String s = info.getParameter(PARAM_FONTSIZE);
		if(DomUtil.isBlank(s))
			k.setFontSize(0);
		else
			k.setFontSize(Integer.parseInt(s));

		s = info.getParameter(PARAM_SPEC);
		FontSpec fs;
		if("b".equalsIgnoreCase(s))
			fs = FontSpec.BOLD;
		else if("i".equalsIgnoreCase(s))
			fs = FontSpec.ITALICS;
		else if("bi".equalsIgnoreCase(s) || "ib".equalsIgnoreCase(s))
			fs = FontSpec.BOLD_ITALICS;
		else if(DomUtil.isBlank(s))
			fs = FontSpec.NORM;
		else
			throw new IllegalArgumentException(s + ": font spec must be empty, i, b or ib");
		k.setFontSpec(fs);
		return k;
	}

	static boolean appendParam(StringBuilder sb, boolean paramExists, String paramName, String value) {
		if(value != null) {
			sb.append(paramExists ? "&" : "?");
			sb.append(paramName + "=");
			StringTool.encodeURLEncoded(sb, value);
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_caption == null) ? 0 : m_caption.hashCode());
		result = prime * result + ((m_color == null) ? 0 : m_color.hashCode());
		result = prime * result + ((m_font == null) ? 0 : m_font.hashCode());
		result = prime * result + m_fontSize;
		result = prime * result + ((m_fontSpec == null) ? 0 : m_fontSpec.hashCode());
		result = prime * result + ((m_icon == null) ? 0 : m_icon.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		MarkerImagePartKey other = (MarkerImagePartKey) obj;
		if(m_caption == null) {
			if(other.m_caption != null)
				return false;
		} else if(!m_caption.equals(other.m_caption))
			return false;
		if(m_color == null) {
			if(other.m_color != null)
				return false;
		} else if(!m_color.equals(other.m_color))
			return false;
		if(m_font == null) {
			if(other.m_font != null)
				return false;
		} else if(!m_font.equals(other.m_font))
			return false;
		if(m_fontSize != other.m_fontSize)
			return false;
		if(m_fontSpec != other.m_fontSpec)
			return false;
		if(m_icon == null) {
			return other.m_icon == null;
		} else
			return m_icon.equals(other.m_icon);
	}

	public void setIcon(String icon) {
		m_icon = icon;
	}

	public String getIcon() {
		return m_icon;
	}

	public void setCaption(String text) {
		m_caption = text;
	}

	public String getCaption() {
		return m_caption;
	}

	void setColor(String color) {
		m_color = color;
	}

	String getColor() {
		return m_color;
	}

	public String getFont() {
		return m_font;
	}

	public void setFont(String font) {
		m_font = font;
	}

	public int getFontSize() {
		return m_fontSize;
	}

	public void setFontSize(int fontSize) {
		m_fontSize = fontSize;
	}

	public FontSpec getFontSpec() {
		return m_fontSpec;
	}

	public void setFontSpec(FontSpec fontSpec) {
		m_fontSpec = fontSpec;
	}
}
