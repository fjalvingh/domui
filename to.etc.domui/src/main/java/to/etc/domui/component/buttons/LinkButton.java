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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.ClickInfo;
import to.etc.domui.dom.html.IActionControl;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.nls.IBundleCode;

import java.util.Objects;

/**
 * A button which looks like a link.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 10, 2008
 */
public class LinkButton extends ATag implements IActionControl {
	@Nullable
	private String m_text;

	@Nullable
	private IIconRef m_icon;

	private boolean m_disabled;

	@Nullable
	private IUIAction<Void> m_action;

	@Nullable
	private Object m_actionInstance;

	@Nullable
	private String m_disabledBecause;

	public LinkButton() {
	}

	public LinkButton(@NonNull String txt, @Nullable IIconRef image, @NonNull IClicked<LinkButton> clk) {
		setClicked(clk);
		setText(txt);
		setImage(image);
	}

	public LinkButton(@NonNull IBundleCode code, @Nullable IIconRef image, @NonNull IClicked<LinkButton> clk) {
		setClicked(clk);
		setText(code.format());
		setImage(image);
	}

	public LinkButton(@NonNull String txt, @NonNull IIconRef image) {
		setText(txt);
		setImage(image);
	}

	public LinkButton(@NonNull IBundleCode code, @NonNull IIconRef image) {
		setText(code.format());
		setImage(image);
	}

	public LinkButton(@NonNull String txt) {
		m_text = txt;
	}

	public LinkButton(@NonNull IBundleCode code) {
		setText(code.format());
	}

	public LinkButton(@NonNull String txt, @NonNull IClicked<LinkButton> clk) {
		setClicked(clk);
		setText(txt);
	}

	public LinkButton(@NonNull IBundleCode code, @NonNull IClicked<LinkButton> clk) {
		setClicked(clk);
		setText(code.format());
	}

	public LinkButton(@NonNull IUIAction<Void> action) throws Exception {
		this();
		m_action = action;
		actionRefresh();
	}

	public LinkButton(@NonNull IUIAction<Void> action, @Nullable Object actionInstance) throws Exception {
		this();
		m_action = action;
		m_actionInstance = actionInstance;
		actionRefresh();
	}

	@Override
	public void createContent() throws Exception {
		addCssClass("ui-lbtn");
		IIconRef icon = m_icon;
		if(icon == null) {
			setBackgroundImage(null);
			addCssClass("ui-lbtn-noi");
			removeCssClass("ui-lbtn-i");
			add(new Span("ui-lbtn-txt", m_text));
		} else {
			addCssClass("ui-lbtn-i");
			removeCssClass("ui-lbtn-noi");
			NodeBase node = icon.createNode();
			add(node);
			node.addCssClass("ui-lbtn-icon");
			add(new Span("ui-lbtn-txt", m_text));
		}
		if(isDisabled())
			addCssClass("ui-disabled");
		else
			removeCssClass("ui-disabled");
	}

	/**
	 * EXPERIMENTAL - UNSTABLE INTERFACE - Refresh the button regarding the state of the action.
	 */
	private void actionRefresh() throws Exception {
		final IUIAction<Object> action = (IUIAction<Object>) getAction();
		if(null == action)
			return;
		String dt = action.getDisableReason(getActionInstance());
		if(null == dt) {
			setTitle(action.getTitle(getActionInstance())); // The default tooltip or remove it if not present
			setDisabled(false);
		} else {
			setTitle(dt);                        // Shot reason for being disabled
			setDisabled(true);
		}
		setText(action.getName(getActionInstance()));
		setImage(action.getIcon(getActionInstance()));
		setClicked((IClicked<LinkButton>) clickednode -> action.execute(LinkButton.this, getActionInstance()));
	}

	public LinkButton setImage(@Nullable IIconRef url) {
		if(DomUtil.isEqual(url, m_icon))
			return this;
		m_icon = url;
		forceRebuild();
		return this;
	}

	public LinkButton icon(IIconRef ref) {
		setImage(ref);
		return this;
	}

	public LinkButton click(IClicked<LinkButton> b) {
		setClicked(b);
		return this;
	}

	public IIconRef getImage() {
		return m_icon;
	}

	@Nullable
	public String getText() {
		return m_text;
	}

	@Override
	public void setText(final @Nullable String txt) {
		m_text = txt;
		forceRebuild();
		if(null != txt)
			setCalculcatedId("lbtn_" + DomUtil.convertToID(txt));
	}

	public LinkButton text(@Nullable String txt) {
		setText(txt);
		return this;
	}

	public LinkButton text(IBundleCode code, Object... param) {
		setText(code.format(param));
		return this;
	}

	@Override
	@NonNull
	public String getComponentInfo() {
		return "LinkButton:" + m_text;
	}

	@Nullable
	public IUIAction<?> getAction() {
		return m_action;
	}

	@Nullable
	public Object getActionInstance() {
		return m_actionInstance;
	}

	public void setAction(@NonNull IUIAction<Void> action) throws Exception {
		if(DomUtil.isEqual(m_action, action))
			return;
		m_action = action;
		actionRefresh();
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		forceRebuild();
	}

	@Override
	public void internalOnClicked(@NonNull ClickInfo cli) throws Exception {
		if(isDisabled())
			return;
		super.internalOnClicked(cli);
	}

	@Nullable
	public String getDisabledBecause() {
		return m_disabledBecause;
	}

	public void setDisabledBecause(@Nullable String msg) {
		if(Objects.equals(msg, m_disabledBecause)) {
			return;
		}
		m_disabledBecause = msg;
		setOverrideTitle(msg);
		setDisabled(msg != null);
	}

	@Override
	public void setHint(String hintText) {
		setTitle(hintText);
	}
}
