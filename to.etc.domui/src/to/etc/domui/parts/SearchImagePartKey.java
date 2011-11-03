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

import to.etc.domui.server.*;
import to.etc.util.*;

/**
 * Key for cache. Used in {@link SearchImagePart}
 * 
 *
 * @author <a href="mailto:btadic@execom.eu">Bojan Tadic</a>
 * Created on Nov 3, 2011
 */
public class SearchImagePartKey {
	static final String PARAM_ICON = "icon";

	static final String PARAM_CAPTION = "caption";

	static final String PARAM_COLOR = "color";

	private String m_icon;

	private String m_caption;

	private String m_color;

	static public SearchImagePartKey decode(IExtendedParameterInfo info) {
		SearchImagePartKey k = new SearchImagePartKey();
		k.setCaption(info.getParameter(PARAM_CAPTION));
		k.setIcon(info.getParameter(PARAM_ICON));
		k.setColor(info.getParameter(PARAM_COLOR));
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
		result = prime * result + ((getColor() == null) ? 0 : getColor().hashCode());
		result = prime * result + ((getIcon() == null) ? 0 : getIcon().hashCode());
		result = prime * result + ((getCaption() == null) ? 0 : getCaption().hashCode());
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
		SearchImagePartKey other = (SearchImagePartKey) obj;
		if(getColor() == null) {
			if(other.getColor() != null)
				return false;
		} else if(!getColor().equals(other.getColor()))
			return false;
		if(getIcon() == null) {
			if(other.getIcon() != null)
				return false;
		} else if(!getIcon().equals(other.getIcon()))
			return false;
		if(getCaption() == null) {
			if(other.getCaption() != null)
				return false;
		} else if(!getCaption().equals(other.getCaption()))
			return false;
		return true;
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

}