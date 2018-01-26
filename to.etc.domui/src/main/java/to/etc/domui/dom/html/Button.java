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

import to.etc.domui.dom.errors.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * The HTML Button tag.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 19, 2008
 */
public class Button extends NodeContainer implements IActionControl, IForTarget {
	private boolean m_disabled;

	private ButtonType m_type = ButtonType.BUTTON;

	private String m_buttonValue;

	private char m_accessKey;

	@Nullable
	private String m_disabledBecause;

	public Button() {
		super("button");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitButton(this);
	}

	@Nonnull @Override public Button css(@Nonnull String... classNames) {
		super.css(classNames);
		return this;
	}

	/**
	 * Do not call click handler when the button is disabled. This prevents a malicious user from clicking a disabled
	 * button and still having an effect.
	 *
	 * @param cli
	 * @throws Exception
	 */
	@Override public void internalOnClicked(@Nonnull ClickInfo cli) throws Exception {
		if(isDisabled()) {						// Disabled buttons should never click, even if browser lies about it
			return;
		}
		super.internalOnClicked(cli);
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled != disabled) {
			changed();
		}
		m_disabled = disabled;
		if(! disabled)
			setOverrideTitle(null);
	}

	/**
	 * Util for updating button enabled / disabled state depending on existence of error (reason for disabling).
	 *
	 * @param rsn reason to disable button. If null, button gets enabled, otherwise it gets disabled with rsn.getMessage() as title (hint)
	 */
	public void setDisabled(@Nullable UIMessage rsn) {
		if(null != rsn) {
			setDisabled(true);
			setOverrideTitle(rsn.getMessage());
			m_disabledBecause = rsn.getMessage();
		} else {
			setDisabled(false);
			setOverrideTitle(null);
			m_disabledBecause = null;
		}
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

	public ButtonType getType() {
		return m_type;
	}

	public void setType(ButtonType type) {
		if(m_type != type)
			changed();
		m_type = type;
	}

	public String getButtonValue() {
		return m_buttonValue;
	}

	public void setButtonValue(String value) {
		if(!DomUtil.isEqual(value, m_buttonValue))
			changed();
		m_buttonValue = value;
	}

	public char getAccessKey() {
		return m_accessKey;
	}

	public void setAccessKey(char accessKey) {
		m_accessKey = accessKey;
	}

	@Nullable @Override public NodeBase getForTarget() {
		return this;
	}
}
