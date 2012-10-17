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

import to.etc.domui.util.*;

/**
 * Limited support for IFrame tags. For now we have only <I>src</I> attribute supported (along with other properties inherited from super classes).
 * FIXME: see what else is needed to fully support IFRAME tag.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 2 Dec 2011
 */
public class IFrame extends NodeBase {
	private String m_src;

	private String m_frameBorder;

	private String m_frameHeight;

	private String m_marginHeight;

	private String m_marginWidth;

	private String m_name;

	private String m_scrolling;

	private String m_frameWidth;

	public IFrame() {
		super("iframe");
	}

	@Override
	public boolean isRendersOwnClose() {
		return true;
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitIFrame(this);
	}

	public String getSrc() {
		return m_src;
	}

	/**
	 * Src attribute of IFRAME.
	 * @param src
	 */
	public void setSrc(String src) {
		if(DomUtil.isEqual(src, m_src))
			return;
		m_src = src;
		changed();
	}

	public String getFrameBorder() {
		return m_frameBorder;
	}

	public void setFrameBorder(String frameBorder) {
		if(DomUtil.isEqual(frameBorder, m_frameBorder))
			return;
		m_frameBorder = frameBorder;
		changed();
	}

	public String getFrameHeight() {
		return m_frameHeight;
	}

	public void setFrameHeight(String frameHeight) {
		if(DomUtil.isEqual(frameHeight, m_frameHeight))
			return;
		m_frameHeight = frameHeight;
		changed();
	}

	public String getMarginHeight() {
		return m_marginHeight;
	}

	public void setMarginHeight(String marginHeight) {
		if(DomUtil.isEqual(marginHeight, m_marginHeight))
			return;
		m_marginHeight = marginHeight;
		changed();
	}

	public String getMarginWidth() {
		return m_marginWidth;
	}

	public void setMarginWidth(String marginWidth) {
		if(DomUtil.isEqual(marginWidth, m_marginWidth))
			return;
		m_marginWidth = marginWidth;
		changed();
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		if(DomUtil.isEqual(name, m_name))
			return;
		m_name = name;
		changed();
	}

	public String getScrolling() {
		return m_scrolling;
	}

	public void setScrolling(String scrolling) {
		if(DomUtil.isEqual(scrolling, m_scrolling))
			return;
		m_scrolling = scrolling;
		changed();
	}

	public String getFrameWidth() {
		return m_frameWidth;
	}

	public void setFrameWidth(String frameWidth) {
		if(DomUtil.isEqual(frameWidth, m_frameWidth))
			return;
		m_frameWidth = frameWidth;
	}
}
