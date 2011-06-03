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
import to.etc.domui.util.*;

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
public class DefaultButton extends Button {
	private String m_text;

	private String m_icon;

	/**
	 * Create an empty button.
	 */
	public DefaultButton() {
		setCssClass("ui-sdbtn");
	}

	/**
	 * Create a button with a text.
	 * @param txt
	 */
	public DefaultButton(final String txt) {
		this();
		setText(txt);
	}

	/**
	 * Create a button with a text and an icon.
	 * @param txt
	 * @param icon
	 */
	public DefaultButton(final String txt, final String icon) {
		this();
		setText(txt);
		setIcon(icon);
	}

	public DefaultButton(final String txt, final IClicked<DefaultButton> clicked) {
		this();
		setText(txt);
		setClicked(clicked);
	}

	public DefaultButton(final String txt, final String icon, final IClicked<DefaultButton> clicked) {
		this();
		setText(txt);
		setIcon(icon);
		setClicked(clicked);
	}

	@Override
	public void createContent() throws Exception {
		Span s = new Span();
		add(s);
		if(null != m_icon) {
			Img img = new Img(m_icon);
			s.add(img);
			img.setImgBorder(0);
		}
		if(!DomUtil.isBlank(m_text))
			decodeAccelerator(m_text, s);
	}

	/**
	 * Uses a resource as the base for the image.
	 * @param resourceBase		The base location for the specified name. Name is resolved with this class as the base.
	 * @param name				The resource's name relative to the class.
	 */
	public void setIconImage(final Class< ? > resourceBase, final String name) {
		setIcon(DomUtil.getJavaResourceRURL(resourceBase, name));
	}

	/**
	 * Sets a (new) icon on this button. This requires an absolute image path.
	 * @param name
	 */
	public void setIcon(final String name) {
		m_icon = name;
		forceRebuild();
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
	public void setText(final String text) {
		m_text = text;
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
					//-- Ends in '!' - treat as liternal 8-/
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
}
