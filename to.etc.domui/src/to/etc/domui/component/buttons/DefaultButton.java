package to.etc.domui.component.buttons;

import to.etc.domui.dom.html.*;
import to.etc.domui.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

/**
 * An HTML button containing a rendered image as the button content. This button creates a button by creating the
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
public class DefaultButton extends Button {
	private final Img			m_img;
	private String		m_propSrc;
	private String		m_text;
	private String		m_icon;

	/**
	 * Create an empty button.
	 */
	public DefaultButton() {
		m_img	= new Img();
		add(m_img);
		m_img.setBorder(0);
		setThemeConfig("defaultbutton.properties");
		setCssClass("ui-ib");
	}

	/**
	 * Create a button with a text.
	 * @param txt
	 */
	public DefaultButton(final String txt) {
		this();
		setButtonText(txt);
	}
	/**
	 * Create a button with a text and an icon.
	 * @param txt
	 * @param icon
	 */
	public DefaultButton(final String txt, final String icon) {
		this();
		setButtonText(txt);
		setIcon(icon);
	}

	public DefaultButton(final String txt, final IClicked<DefaultButton> clicked) {
		this();
		setButtonText(txt);
		setClicked(clicked);
	}
	public DefaultButton(final String txt, final String icon, final IClicked<DefaultButton> clicked) {
		this();
		setButtonText(txt);
		setIcon(icon);
		setClicked(clicked);
	}

	/**
	 * Set the rendering properties file to be used to render the button image. Use an absolute path to the
	 * properties file. This overrides the default properties file which is 'defaultbutton.properties' in the
	 * current theme.
	 * @param src
	 */
	public void		setConfig(final String src) {
		m_propSrc = src;
		genURL();
	}

	/**
	 * Set the rendering properties file to be used to render the button image, as a class
	 * resource. This overrides the default properties file which is 'defaultbutton.properties' in the
	 * current theme.
	 * @param resourceBase
	 * @param name
	 */
	public void		setConfig(final Class<?> resourceBase, final String name) {
		String rb = resourceBase.getName();
		int pos = rb.lastIndexOf('.');
		if(pos == -1)
			throw new IllegalStateException("??");
		m_propSrc = Constants.RESOURCE_PREFIX+rb.substring(0, pos+1).replace('.', '/')+name;
		genURL();
	}

	/**
	 * Set the rendering properties file to be used to render the button image. The name must
	 * refer to a file in the current theme. This overrides the default properties file which
	 * is 'defaultbutton.properties' in the current theme.
	 * @param name
	 */
	public void	setThemeConfig(final String name) {
		m_propSrc = PageContext.getRequestContext().getRelativeThemePath(name);
		genURL();
	}

	/**
	 * Uses a resource as the base for the image.
	 * @param resourceBase		The base location for the specified name. Name is resolved with this class as the base.
	 * @param name				The resource's name relative to the class.
	 */
	public void	setIconImage(final Class<?> resourceBase, final String name) {
		m_icon = DomUtil.getJavaResourceRURL(resourceBase, name);
		genURL();
	}

	/**
	 * Sets a (new) icon on this button. This requires an absolute image path.
	 * @param name
	 */
	public void	setIcon(final String name) {
		m_icon = PageContext.getRequestContext().translateResourceName(name);
		genURL();
	}

//	/**
//	 * Sets a (new) icon on this button obtained from the current theme's directory.
//	 * @param name		The filename only of the image to render.
//	 */
//	public void	setThemeIcon(String name) {
//		m_icon = "/"+PageContext.getRequestContext().getRelativeThemePath(name);
//		genURL();
//	}

	private void	genURL() {
		StringBuilder	sb	= new StringBuilder(128);
		sb.append(PropBtnPart.class.getName());
		sb.append(".part?src=");
		sb.append(m_propSrc);
		if(m_text != null) {
			sb.append("&amp;txt=");
			StringTool.encodeURLEncoded(sb, m_text);
		}
		if(m_icon != null) {
			sb.append("&amp;icon=");
			StringTool.encodeURLEncoded(sb, m_icon);
		}
		m_img.setSrc(sb.toString());
	}

	/**
	 * Returns the text currently set on the button.
	 * @return
	 */
	public String getLiteralText() {
		return m_text;
	}

	/**
	 * Set a (new) text on the button's surface. The text may contain a '!' to specify an accelerator. To
	 * actually render the exclamation point precede it with a backslash.
	 *
	 * @see to.etc.domui.dom.html.NodeContainer#setButtonText(java.lang.String)
	 */
	@Override
	public void setButtonText(final String text) {
		m_text = text;
		decodeAccelerator(text);
		genURL();
	}
	@Override
	public void	setText(final BundleRef ref, final String key) {
		setButtonText( ref.getString(key) );
	}

	private void	decodeAccelerator(final String txt) {
		int	ix	= 0;
		int	len	= txt.length();
		while(ix < len) {
			int	pos	= txt.indexOf('!', ix);
			if(pos == -1)
				return;
			if(pos > 0 && txt.charAt(pos-1) == '\\') {
				//-- Escaped. Try next one.
				ix	= pos+1;
			} else {
				if(pos+1 >= len)
					return;
				char c = txt.charAt(pos+1);
				if(Character.isLetter(c)) {
					c	= Character.toLowerCase(c);
					setAccessKey(c);
					return;
				}
			}
		}
	}
}
