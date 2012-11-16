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

import to.etc.domui.dom.html.*;

/**
 * A Button tag containing a single, usually small, image. The image is a normal image
 * resource and not in any way changed by the server. This button type is typically used
 * as an icon button after some input, or as part of a toolbar.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 17, 2008
 */
public class SmallImgButton extends Button {
	private Img m_image;

	/**
	 * Create the empty button.
	 */
	public SmallImgButton() {
		m_image = new Img();
		add(m_image);
		setCssClass("ui-sib");
	}

	/**
	 * Create a small image button from the specified resource. The resource can come from the current
	 * theme, or it can be an absolute image path to a web file.
	 * @param intheme
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

	//	/**
	//	 * Create a small image button from the specified resource. The resource can come from the current
	//	 * theme, or it can be an absolute image path to a web file.
	//	 * @param intheme
	//	 * @param rurl
	//	 */
	//	public SmallImgButton(boolean intheme, String rurl, IClicked<SmallImgButton> cl) {
	//		this();
	//		setClicked(cl);
	//		if(intheme)
	//			setThemeSrc(rurl);
	//		else
	//			setSrc(rurl);
	//	}

	/**
	 * Set a new image using a web resource's abolute path. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public void setSrc(String src) {
		m_image.setSrc(src);
	}
	//
	//	/**
	//	 * Set a new image from the current theme.
	//	 * @param src
	//	 */
	//	public void setThemeSrc(String src) {
	//		m_image.setThemeSrc(src);
	//	}

	public String getSrc() {
		return m_image.getSrc();
	}

	@Override
	public String getComponentInfo() {
		return "ImgButton:" + m_image.getSrc();
	}
}
