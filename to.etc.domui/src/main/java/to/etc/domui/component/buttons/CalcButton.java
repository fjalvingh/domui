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

import to.etc.domui.dom.html.Button;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Page;
import to.etc.domui.parts.ButtonPartKey;
import to.etc.domui.parts.PropBtnPart;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.parts.PartData;
import to.etc.domui.server.parts.PartService;
import to.etc.domui.util.DomUtil;
import to.etc.util.WrappedException;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * This was the "DefaultButton" until june 2011.
 *
 * <p>An HTML button containing a rendered image as the button content. This button creates a button by creating the
 * full visible presence of the button as a server-side rendered image. The button can contain a text, an icon or
 * both, and things like the text color, font and style can be manipulated. The actual rendering process uses a
 * properties file 'defaultbutton.properties' present in the <i>current</i> theme. This property file contains
 * all of the basic rendering options for rendering the button, like:
 * <ul>
 * 	<li>What is the base image for the button (the image of the button without texts)</li>
 *	<li>What relative location is any optional icon placed? Left or right?</li>
 *	<li>What is the spacing between icon and text, and text and image</li>
 * </ul>
 * etc, etc.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 21, 2008
 */
@Deprecated
public class CalcButton extends Button {
	private ButtonPartKey m_key = new ButtonPartKey();

	/**
	 * Create an empty button.
	 */
	public CalcButton() {
		setConfig("THEME/defaultbutton.properties");
		setCssClass("ui-dbtn");
	}

	/**
	 * Create a button with a text.
	 * @param txt
	 */
	public CalcButton(final String txt) {
		this();
		setText(txt);
	}

	/**
	 * Create a button with a text and an icon.
	 * @param txt
	 * @param icon
	 */
	public CalcButton(final String txt, final String icon) {
		this();
		setText(txt);
		setIcon(icon);
	}

	public CalcButton(final String txt, final IClicked<CalcButton> clicked) {
		this();
		setText(txt);
		setClicked(clicked);
	}

	public CalcButton(final String txt, final String icon, final IClicked<CalcButton> clicked) {
		this();
		setText(txt);
		setIcon(icon);
		setClicked(clicked);
	}

	/**
	 * Set the rendering properties file to be used to render the button image. Use an absolute path to the
	 * properties file. This overrides the default properties file which is 'defaultbutton.properties' in the
	 * current theme.
	 * @param src
	 */
	public void setConfig(final String src) {
		m_key.setPropFile(src);
		genURL();
	}

	/**
	 * Set the rendering properties file to be used to render the button image, as a class
	 * resource. This overrides the default properties file which is 'defaultbutton.properties' in the
	 * current theme.
	 * @param resourceBase
	 * @param name
	 */
	public void setConfig(final Class< ? > resourceBase, final String name) {
		setConfig(DomUtil.getJavaResourceRURL(resourceBase, name));
	}

	/**
	 * Uses a resource as the base for the image.
	 * @param resourceBase		The base location for the specified name. Name is resolved with this class as the base.
	 * @param name				The resource's name relative to the class.
	 */
	public void setIconImage(final Class< ? > resourceBase, final String name) {
		m_key.setIcon(DomUtil.getJavaResourceRURL(resourceBase, name));
		genURL();
	}

	/**
	 * Sets a (new) icon on this button. This requires an absolute image path.
	 * @param name
	 */
	public void setIcon(final String name) {
		m_key.setIcon(name);
		genURL();
	}

	/**
	 * Generate the URL to the button renderer. Since things like the button text can contain
	 * tilded resource keys we cannot generate the URL when we're not attached to a page; in
	 * that case we ignore the call and generate the URL at page attachment time.
	 */
	private void genURL() {
		if(getPage() == null)							// Not attached yet?
			return;

		StringBuilder sb = new StringBuilder(128);
		m_key.append(sb);
		setBackgroundImage(sb.toString());

		//-- Determine image size: force it generated and use the cached copy for sizing
		PartService ph = DomApplication.get().getPartService();
		try {
			PartData ci = ph.getCachedInstance(PropBtnPart.INSTANCE, m_key);
			Dimension d = (Dimension) ci.getExtra();
			setWidth(d.width + "px");
			setHeight(d.height + "px");
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	/**
	 * When attached to a page, this causes the Button Image Renderer URL to be
	 * set in the image. It can only be done when the button is attached because
	 * the button can contain tilde-escaped keys.
	 *
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		super.onAddedToPage(p);
		genURL();
	}

	/**
	 * Returns the text currently set on the button.
	 * @return
	 */
	public String getText() {
		return m_key.getText();
	}

	/**
	 * Set a (new) text on the button's surface. The text may contain a '!' to specify an accelerator. To
	 * actually render the exclamation point precede it with a backslash.
	 *
	 * @see to.etc.domui.dom.html.NodeContainer#setText(java.lang.String)
	 */
	@Override
	public void setText(final @Nullable String text) {
		m_key.setText(text);
		decodeAccelerator(text);
		genURL();
	}

	private void decodeAccelerator(final String txt) {
		int ix = 0;
		int len = txt.length();
		while(ix < len) {
			int pos = txt.indexOf('!', ix);
			if(pos == -1)
				return;
			if(pos > 0 && txt.charAt(pos - 1) == '\\') {
				//-- Escaped. Try next one.
				ix = pos + 1;
			} else {
				if(pos + 1 >= len)
					return;
				char c = txt.charAt(pos + 1);
				if(Character.isLetter(c)) {
					c = Character.toLowerCase(c);
					setAccessKey(c);
					return;
				}
				pos += 2;
			}
		}
	}
}
