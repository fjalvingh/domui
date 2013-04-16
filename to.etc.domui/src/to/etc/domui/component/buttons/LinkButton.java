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

import javax.annotation.*;

import to.etc.domui.component.menu.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * A button which looks like a link.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 10, 2008
 */
public class LinkButton extends ATag implements IActionControl {
	private String m_text;

	private String m_imageUrl;

	private boolean m_disabled;

	private IUIAction<Void> m_action;

	public LinkButton() {
		setCssClass("ui-lnkb");
	}

	public LinkButton(final String txt, final String image, final IClicked<LinkButton> clk) {
		setCssClass("ui-lnkb ui-lbtn");
		setClicked(clk);
		m_text = txt;
		setImage(image);
	}

	public LinkButton(final String txt, final String image) {
		setCssClass("ui-lnkb ui-lbtn");
		m_text = txt;
		setImage(image);
	}

	public LinkButton(final String txt) {
		setCssClass("ui-lnkb");
		m_text = txt;
	}

	public LinkButton(final String txt, final IClicked<LinkButton> clk) {
		setCssClass("ui-lnkb");
		setClicked(clk);
		m_text = txt;
	}

	public LinkButton(IUIAction<Void> action) throws Exception {
		this();
		m_action = action;
		actionRefresh();
	}

	@Override
	public void createContent() throws Exception {
		setText(m_text);
	}

	/**
	 * EXPERIMENTAL - UNSTABLE INTERFACE - Refresh the button regarding the state of the action.
	 */
	private void actionRefresh() throws Exception {
		final IUIAction< ? > action = getAction();
		if(null == action)
			return;
		String dt = action.getDisableReason(null);
		if(null == dt) {
			setTitle(action.getTitle(null)); // The default tooltip or remove it if not present
			setDisabled(false);
		} else {
			setTitle(dt);						// Shot reason for being disabled
			setDisabled(true);
		}
		setText(action.getName(null));
		setImage(action.getIcon(null));
		setClicked(new IClicked<LinkButton>() {
			@Override
			public void clicked(LinkButton clickednode) throws Exception {
				action.execute(LinkButton.this, null);
			}
		});
	}

	public void setImage(final String url) {
		if(DomUtil.isEqual(url, m_imageUrl))
			return;
		m_imageUrl = url;
		updateStyle();
		forceRebuild();
	}

	public String getImage() {
		return m_imageUrl;
	}

	private void updateStyle() {
		if(m_imageUrl == null) {
			setBackgroundImage(null);
			setCssClass("ui-lnkb");
		} else {
			setBackgroundImage(DomApplication.get().getThemedResourceRURL(m_imageUrl));
			setCssClass("ui-lnkb ui-lbtn");
		}
		if(isDisabled())
			addCssClass("ui-lnkb-dis");
		else
			removeCssClass("ui-lnkb-dis");
	}

	@Override
	public void setText(final @Nullable String txt) {
		m_text = txt;
		super.setText(txt);
	}

	@Override
	public String getComponentInfo() {
		return "LinkButton:" + m_text;
	}

	public IUIAction<Void> getAction() {
		return m_action;
	}

	public void setAction(IUIAction<Void> action) throws Exception {
		if(DomUtil.isEqual(m_action, action))
			return;
		m_action = action;
		actionRefresh();
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		updateStyle();
		forceRebuild();
	}

	@Override
	public void internalOnClicked(ClickInfo cli) throws Exception {
		if(isDisabled())
			return;
		super.internalOnClicked(cli);
	}
}
