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
package to.etc.domui.dom.html;

import to.etc.domui.util.DomUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The base tag for an Image.
 *
 * <h2>Tips and tricks</h2>
 * <p>To prevent the butt-ugly border on any image placed in an A tag add the following rule to the CSS:
 *  <pre>
 *  a img {
 *      border: medium none;
 *  }
 *  </pre>
 * </p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 4, 2008
 */
public class Img extends NodeBase implements IActionControl {
	private String m_alt;

	private String m_src;

	private ImgAlign m_align;

	private int m_imgBorder = -1;

	private String m_imgHeight;

	private String m_imgWidth;

	private boolean m_disabled;

	private String m_useMap;

	/**
	 * Creates an uninitialized image.
	 */
	public Img() {
		super("img");
	}

	/**
	 * Creates an image with the specified source. This should be an absolute web resource path.  If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public Img(String src) {
		this();
		setSrc(src);
		setImgBorder(0);
	}

	/**
	 * Creates an image with the specified source. This should be an absolute web resource path.  If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public Img(Class<?> base, String src) {
		this();
		setSrc(base, src);
		setImgBorder(0);
	}

	//	/**
	//	 * Creates an image with the specified source. This can be theme-relative or it can be an
	//	 * absolute web resource path.
	//	 * @param themesrc
	//	 * @param src
	//	 */
	//	public Img(boolean themesrc, String src) {
	//		this();
	//		if(themesrc)
	//			setThemeSrc(src);
	//		else
	//			setSrc(src);
	//		setImgBorder(0);
	//	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitImg(this);
	}

	/**
	 * Return the current "alt" string of the image.
	 * @return
	 */
	public String getAlt() {
		return m_alt;
	}

	/**
	 * Set the "alt" string of the image.
	 * @param alt
	 */
	public void setAlt(String alt) {
		if(!DomUtil.isEqual(alt, m_alt))
			changed();
		m_alt = alt;
	}

	/**
	 * Get the current source for the image as an absolute web app path.
	 * @return
	 */
	public String getSrc() {
		return m_src;
	}

	/**
	 * Set the source for this image as an absolute web app path. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public void setSrc(String src) {
		if(!DomUtil.isEqual(src, m_src))
			changed();
		m_src = src;
	}

	/**
	 * Set the source as a Java resource based off the given class.
	 * @param base
	 * @param resurl
	 */
	public void setSrc(Class<?> base, String resurl) {
		String s = DomUtil.getJavaResourceRURL(base, resurl);
		setSrc(s);
	}

	public ImgAlign getAlign() {
		return m_align;
	}

	public void setAlign(ImgAlign align) {
		if(m_align != align)
			changed();
		m_align = align;
	}

	public int getImgBorder() {
		return m_imgBorder;
	}

	public void setImgBorder(int imgBorder) {
		if(m_imgBorder != imgBorder)
			changed();
		m_imgBorder = imgBorder;
	}

	public String getImgHeight() {
		return m_imgHeight;
	}

	public void setImgHeight(String imgHeight) {
		if(DomUtil.isEqual(m_imgHeight, imgHeight))
			return;
		changed();
		m_imgHeight = imgHeight;
	}

	public String getImgWidth() {
		return m_imgWidth;
	}

	public void setImgWidth(String imgWidth) {
		if(DomUtil.isEqual(m_imgWidth, imgWidth))
			return;
		changed();
		m_imgWidth = imgWidth;
	}

	@Override
	public void setClicked(@Nullable IClicked<?> clicked) {
		super.setClicked(clicked);
		if(null != clicked) {
			addCssClass("ui-clickable");
		} else {
			removeCssClass("ui-clickable");
		}
	}

	@Override public void setClicked2(IClicked2<?> clicked) {
		super.setClicked2(clicked);
		if(null != clicked) {
			addCssClass("ui-clickable");
		} else {
			removeCssClass("ui-clickable");
		}
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	/**
	 * When disabled the image renders by greying out the image.
	 */
	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		changed();
	}

	@Override
	public void internalOnClicked(@Nonnull ClickInfo cli) throws Exception {
		if(isDisabled())
			return;
		super.internalOnClicked(cli);
	}

	public String getUseMap() {
		return m_useMap;
	}

	public void setUseMap(String useMap) {
		if(Objects.equals(m_useMap, useMap))
			return;
		m_useMap = useMap;
		changed();
	}
}
