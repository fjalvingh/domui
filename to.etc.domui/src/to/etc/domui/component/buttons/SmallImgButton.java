package to.etc.domui.component.buttons;

import to.etc.domui.dom.html.*;

/**
 * A Button tag containing a single, usually small, image. The image is a normal image
 * resource and not in any way changed by the server. This button type is typically used
 * as an icon button after some input, or as part of a toolbar.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 17, 2008
 */
public class SmallImgButton extends Button {
	private Img			m_image;

	/**
	 * Create the empty button.
	 */
	public SmallImgButton() {
		m_image = new Img();
		add(m_image);
		setCssClass("ui-sib");
	}

	/**
	 * Create a small image button from the specified resource. The resource can come from the current
	 * theme, or it can be an absolute image path to a web file.
	 * @param intheme
	 * @param rurl
	 */
	public SmallImgButton(String rurl) {
		this();
		setSrc(rurl);
	}

	/**
	 * If the rurl is prefixed with THEME/ it specifies an image from the current THEME's directory.
	 * @param rurl
	 * @param cl
	 */
	public SmallImgButton(String rurl, IClicked<SmallImgButton> cl) {
		this();
		setClicked(cl);
		setSrc(rurl);
	}

//	/**
//	 * Create a small image button from the specified resource. The resource can come from the current
//	 * theme, or it can be an absolute image path to a web file.
//	 * @param intheme
//	 * @param rurl
//	 */
//	public SmallImgButton(boolean intheme, String rurl, IClicked<SmallImgButton> cl) {
//		this();
//		setClicked(cl);
//		if(intheme)
//			setThemeSrc(rurl);
//		else
//			setSrc(rurl);
//	}

	/**
	 * Set a new image using a web resource's abolute path. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public void setSrc(String src) {
		m_image.setSrc(src);
	}
//
//	/**
//	 * Set a new image from the current theme.
//	 * @param src
//	 */
//	public void setThemeSrc(String src) {
//		m_image.setThemeSrc(src);
//	}
}
