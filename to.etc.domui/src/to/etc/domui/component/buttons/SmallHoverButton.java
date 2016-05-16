package to.etc.domui.component.buttons;

import to.etc.domui.dom.html.*;

/**
 * DO NOT USE - replaced by {@link HoverButton}.
 * jal 2015/09/28 This does not properly implement the action interface and lacks for instance "disabled".
 */
@Deprecated
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
	 * Set a new image using a web resource's absolute path. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public void setSrc(String src) {
		m_url = src;
		setBackgroundImage(src);
	}
}
