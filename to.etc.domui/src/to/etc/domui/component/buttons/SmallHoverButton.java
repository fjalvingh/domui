package to.etc.domui.component.buttons;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

public class SmallHoverButton extends Button {
	private String m_url;

	/**
	 * Create the empty button.
	 */
	public SmallHoverButton() {
		setCssClass("ui-smhb");
	}

	/**
	 * Create a small image button from the specified resource. The resource can come from the current
	 * theme, or it can be an absolute image path to a web file.
	 * @param intheme
	 * @param rurl
	 */
	public SmallHoverButton(String rurl) {
		this();
		setSrc(rurl);
	}

	/**
	 * If the rurl is prefixed with THEME/ it specifies an image from the current THEME's directory.
	 * @param rurl
	 * @param cl
	 */
	public SmallHoverButton(String rurl, IClicked<SmallHoverButton> cl) {
		this();
		setClicked(cl);
		setSrc(rurl);
	}

	/**
	 * Set a new image using a web resource's abolute path. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public void setSrc(String src) {
		m_url = src;
		setBackgroundImage(DomApplication.get().getThemedResourceRURL(src));
	}
}
