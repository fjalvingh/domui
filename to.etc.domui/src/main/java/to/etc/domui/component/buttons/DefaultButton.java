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
import to.etc.domui.component.misc.IIcon;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Button;
import to.etc.domui.dom.html.IActionControl;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.Underline;
import to.etc.domui.util.DomUtil;
import to.etc.util.StringTool;

import java.util.Objects;

/**
 * The default button for DomUI renders a sliding doors button that can
 * contain a text and an icon. The button is fully styled through CSS.
 * The rendered structure is as follows:
 * <pre>
 * 	&lt;button type='button' class='ui-sdbtn' onclick=... accesskey=...>
 * 		&lt;span>
 * 			&lt;img src="icon" border="0">
 * 			&lt;u>l&lt;/u>abel text
 * 		&lt;span>
 * 	&lt;/button>
 * </pre>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 3, 2011
 */
public class DefaultButton extends Button implements IActionControl {
	private String m_text;

	@Nullable
	private IIcon m_icon;

	/** If this is an action-based button this contains the action. */
	private IUIAction<?> m_action;

	private Object m_actionInstance;

	/**
	 * Create an empty button.
	 */
	public DefaultButton() {
		//addCssClass("xxui-sdbtn");
		addCssClass("ui-button");
		addCssClass("ui-control");
	}

	/**
	 * Create a button with a text.
	 */
	public DefaultButton(String txt) {
		this();
		setText(txt);
	}

	/**
	 * Create a {@link IUIAction} based button.
	 */
	public DefaultButton(IUIAction<?> action) throws Exception {
		this();
		m_action = action;
		actionRefresh();
	}

	/**
	 * Create a {@link IUIAction} based button.
	 */
	public <T> DefaultButton(T instance, IUIAction<T> action) throws Exception {
		this();
		m_action = action;
		m_actionInstance = instance;
		actionRefresh();
	}


	/**
	 * Create a button with a text and an icon.
	 */
	public DefaultButton(String txt, IIcon icon) {
		this();
		setText(txt);
		setIcon(icon);
	}

	public DefaultButton(String txt, IClicked<DefaultButton> clicked) {
		this();
		setText(txt);
		setClicked(clicked);
	}

	public DefaultButton(String txt, IIcon icon, final IClicked<DefaultButton> clicked) {
		this();
		setText(txt);
		setIcon(icon);
		setClicked(clicked);
	}

	/**
	 * Add the specified css class(es) to the button.
	 */
	@NonNull
	@Override
	public DefaultButton css(String... classNames) {
		super.css(classNames);
		return this;
	}

	/**
	 * Set the optional text (which may include an accelerator).
	 */
	@NonNull
	public DefaultButton text(String text) {
		setText(text);
		return this;
	}

	@NonNull
	public DefaultButton icon(IIcon icon) {
		setIcon(icon);
		return this;
	}

	@NonNull
	public DefaultButton clicked(IClicked<DefaultButton> on) {
		setClicked(on);
		return this;
	}

	@Override
	public void createContent() throws Exception {
		IIcon iconUrl = m_icon;
		if(null != iconUrl) {
			Span iconSpan = new Span();
			add(iconSpan);
			iconSpan.setCssClass("ui-icon");
			NodeBase node = iconUrl.createNode();
			iconSpan.add(node);
			//if(DomUtil.isIconName(iconUrl)) {
			//	FontIcon icon = new FontIcon(iconUrl);
			//	//icon.addCssClass("xxui-sdbtn-icon ui-icon");
			//	iconSpan.add(icon);
			//} else {
			//	String icon = getThemedResourceRURL(iconUrl);
			//	Img img = new Img(icon);
			//	iconSpan.add(img);
			//	img.setImgBorder(0);
			//	img.setDisabled(isDisabled());
			//}
		}
		if(! StringTool.isBlank(getText())) {
			Span txt = new Span();
			txt.setCssClass("xxui-sdbtn-txt");
			add(txt);
			if(!DomUtil.isBlank(m_text))
				decodeAccelerator(m_text, txt);
		}
	}

	/**
	 * Define this as a "mini" button, usable to be added inside a table row.
	 */
	public DefaultButton mini() {
		setCssClass("ui-sdbtn-mini");
		return this;
	}

	/**
	 * Uses a resource as the base for the image.
	 * @param resourceBase        The base location for the specified name. Name is resolved with this class as the base.
	 * @param name                The resource's name relative to the class.
	 */
	public void setIconImage(final Class<?> resourceBase, final String name) {
		setIcon(Icon.of(DomUtil.getJavaResourceRURL(resourceBase, name)));
	}

	/**
	 * Sets a (new) icon on this button. This requires an absolute image path.
	 */
	public void setIcon(@Nullable IIcon name) {
		m_icon = name;
		forceRebuild();
	}

	@Nullable
	public IIcon getIcon() {
		return m_icon;
	}

	/**
	 * Returns the text currently set on the button.
	 * @return
	 */
	public String getText() {
		return m_text;
	}

	/**
	 * Set a (new) text on the button's surface. The text may contain a '!' to specify an accelerator. To
	 * actually render the exclamation point precede it with a backslash.
	 *
	 * @see to.etc.domui.dom.html.NodeContainer#setText(java.lang.String)
	 */
	@Override
	public void setText(final @Nullable String text) {
		if(Objects.equals(text, m_text))
			return;
		m_text = text;
		if(null != text)
			setCalculcatedId("button_" + DomUtil.convertToID(text));
		forceRebuild();
	}

	/**
	 * Decode the text string and split it to put accelerator
	 * in button and text in span.
	 *
	 * @param txt
	 */
	private void decodeAccelerator(final String txt, Span into) {
		StringBuilder sb = new StringBuilder(txt.length());
		int ix = 0;
		int len = txt.length();
		while(ix < len) {
			int pos = txt.indexOf('!', ix);
			if(pos == -1) {
				//-- Append the remainder
				sb.append(txt.substring(ix));
				if(sb.length() > 0)
					into.add(sb.toString());
				return;
			}

			if(pos > 0 && txt.charAt(pos - 1) == '\\') {
				//-- Escaped with \ - add the part until just before the \\.
				sb.append(txt, ix, pos - 1); // Copy excluding backslash
				sb.append('!'); // Then add !
				ix = pos + 1; // Just after !
			} else {
				//-- Got an unescaped !. Add the part leading to it,
				if(pos > ix)
					sb.append(txt, ix, pos);

				if(pos + 1 >= len) {
					//-- Ends in '!' - treat as literal 8-/
					sb.append('!');
					into.add(sb.toString());
					return;
				}

				//-- We have a probable accellerator.
				char c = txt.charAt(pos + 1);
				if(Character.isLetterOrDigit(c)) {
					c = Character.toLowerCase(c);
					setAccessKey(c);
				}

				if(sb.length() > 0) {
					into.add(sb.toString());
					sb.setLength(0);
				}

				//-- Accelerator chars are marked in a special way.
				Underline ac = new Underline();
				into.add(ac);
				ac.add(txt.substring(pos + 1, pos + 2));
				ix = pos + 2;
			}
		}
	}

	@Override
	public String getComponentInfo() {
		return "Button:" + m_text;
	}

	@Override
	public void setDisabled(boolean disabled) {
		super.setDisabled(disabled);
		forceRebuild();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	EXPERIMENTAL - UNSTABLE INTERFACE					*/
	/*--------------------------------------------------------------*/

	/**
	 * EXPERIMENTAL - UNSTABLE INTERFACE - Get the action associated with this button, or
	 * null if the button is not action based.
	 * @return
	 */
	@Nullable
	public IUIAction<?> getAction() {
		return m_action;
	}

	/**
	 * EXPERIMENTAL - UNSTABLE INTERFACE - Refresh the button regarding the state of the action.
	 */
	private void actionRefresh() throws Exception {
		final IUIAction<Object> action = (IUIAction<Object>) getAction();
		if(null == action)
			return;
		String dt = action.getDisableReason(m_actionInstance);
		if(null == dt) {
			dt = action.getTitle(m_actionInstance);        // The default tooltip or remove it if not present
			setDisabled(false);
		} else {
			setDisabled(true);
		}
		setTitle(dt);
		setText(action.getName(m_actionInstance));
		setIcon(action.getIcon(m_actionInstance));
		setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(@NonNull DefaultButton clickednode) throws Exception {
				action.execute(DefaultButton.this, m_actionInstance);
			}
		});
	}
}
