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
package to.etc.domui.component.buttons;

import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.dom.html.Button;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Img;
import to.etc.domui.util.DomUtil;

/**
 * A Button tag containing a single, usually small, image. The image is a normal image
 * resource and not in any way changed by the server. This button type is typically used
 * as an icon button after some input, or as part of a toolbar.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 17, 2008
 */
public class SmallImgButton extends Button {
	private String m_icon;

	/**
	 * Create the empty button.
	 */
	public SmallImgButton() {
		setCssClass("ui-sib");
	}

	/**
	 * Create a small image button from the specified resource. The resource can come from the current
	 * theme, or it can be an absolute image path to a web file.
	 * @param rurl
	 */
	public SmallImgButton(String rurl) {
		this();
		setSrc(rurl);
	}

	/**
	 * If the rurl is prefixed with THEME/ it specifies an image from the current THEME's directory.
	 * @param rurl
	 * @param cl
	 */
	public SmallImgButton(String rurl, IClicked<SmallImgButton> cl) {
		this();
		setClicked(cl);
		setSrc(rurl);
	}

	/**
	 * Set a new image using a web resource's absolute path. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public void setSrc(String src) {
		m_icon = src;
		forceRebuild();
	}

	public String getSrc() {
		return m_icon;
	}

	@Override
	public String getComponentInfo() {
		return "ImgButton:" + m_icon;
	}

	@Override
	public void createContent() throws Exception {
		String iconUrl = m_icon;
		if(null != iconUrl) {
			//-- Does the URL contain a dot? That indicates a resource somehow.
			if(DomUtil.isIconName(iconUrl)) {
				FaIcon icon = new FaIcon(iconUrl);
				icon.addCssClass("ui-sib-icon");
				add(icon);
			} else {
				String icon = getThemedResourceRURL(iconUrl);
				Img img = new Img(icon);
				add(img);
				img.setImgBorder(0);
				img.setDisabled(isDisabled());
			}
		}
	}
}
