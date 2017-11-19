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
import to.etc.domui.state.UIContext;
import to.etc.util.StringTool;

public class ButtonPartKey {
	private String m_propfile;

	private String m_icon;

	private String m_text;

	private String m_color;

	private String m_img;

	static public ButtonPartKey decode(IExtendedParameterInfo info) {
		ButtonPartKey k = new ButtonPartKey();
		k.setPropFile(info.getParameter("src"));
		k.setText(info.getParameter("txt"));
		k.setIcon(info.getParameter("icon"));
		k.setColor(info.getParameter("color"));
		k.setImg(info.getParameter("img"));
		return k;
	}

	public void append(StringBuilder sb) {
		sb.append(PropBtnPart.class.getName());
		sb.append(".part?src=");
		String propfile = m_propfile;
		sb.append(DomApplication.get().internalGetThemeManager().getThemedResourceRURL(UIContext.getRequestContext(), propfile));
		if(m_text != null) {
			sb.append("&txt=");
			//			String text = DomUtil.replaceTilded(this, m_text);
			StringTool.encodeURLEncoded(sb, m_text);
		}
		String icon = m_icon;
		if(icon != null) {
			sb.append("&icon=");
			StringTool.encodeURLEncoded(sb, DomApplication.get().internalGetThemeManager().getThemedResourceRURL(UIContext.getRequestContext(), icon));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getColor() == null) ? 0 : getColor().hashCode());
		result = prime * result + ((getIcon() == null) ? 0 : getIcon().hashCode());
		result = prime * result + ((getImg() == null) ? 0 : getImg().hashCode());
		result = prime * result + ((getPropFile() == null) ? 0 : getPropFile().hashCode());
		result = prime * result + ((getText() == null) ? 0 : getText().hashCode());
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
		ButtonPartKey other = (ButtonPartKey) obj;
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
		if(getImg() == null) {
			if(other.getImg() != null)
				return false;
		} else if(!getImg().equals(other.getImg()))
			return false;
		if(getPropFile() == null) {
			if(other.getPropFile() != null)
				return false;
		} else if(!getPropFile().equals(other.getPropFile()))
			return false;
		if(getText() == null) {
			return other.getText() == null;
		} else
			return getText().equals(other.getText());
	}

	public void setPropFile(String propfile) {
		m_propfile = propfile;
	}

	public String getPropFile() {
		return m_propfile;
	}

	public void setIcon(String icon) {
		m_icon = icon;
	}

	public String getIcon() {
		return m_icon;
	}

	public void setText(String text) {
		m_text = text;
	}

	public String getText() {
		return m_text;
	}

	public void setColor(String color) {
		m_color = color;
	}

	public String getColor() {
		return m_color;
	}

	public void setImg(String img) {
		m_img = img;
	}

	public String getImg() {
		return m_img;
	}
}
