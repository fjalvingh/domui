package to.etc.domui.component.buttons;

import to.etc.domui.component.image.Dimension;
import to.etc.domui.dom.html.Button;
import to.etc.domui.dom.html.IActionControl;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.server.DomApplication;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is a button that has hover functionality to handle the button's
 * click effects. The effects take place at hover and at focus. The image
 * resource that is required for a button like this must consist of a single
 * image with the size 48x16. This image consists of three icons of size 16x16
 * next to each other in the order:
 * <ol>
 *	<li>Normal icon</li>
 *	<li>Hover/focus icon</li>
 *	<li>Disabled icon</li>
 * </ol>
 *
 * <p>The button can have several sizes but defaults to 16x16 (small). Other sizes are medium (24x24) and large (32x32)</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9/28/15.
 */
@DefaultNonNull
final public class HoverButton extends Button implements IActionControl {
	public enum Size {
		/** Standard 16x16 button */
		SMALL
		/** Medium size 24x24 button */
		, MEDIUM
		/** Large 32x32 button */
		, LARGE
	}

	@Nullable
	private String m_url;

	/**
	 * Create the empty button.
	 */
	public HoverButton() {
	}

	/**
	 * Create a small image button from the specified resource. The resource can come from the current
	 * theme, or it can be an absolute image path to a web file.
	 * @param rurl
	 */
	public HoverButton(@Nonnull String rurl) {
		setSrc(rurl);
	}

	/**
	 * If the rurl is prefixed with THEME/ it specifies an image from the current THEME's directory.
	 * @param rurl
	 * @param cl
	 */
	public HoverButton(@Nonnull String rurl, @Nonnull IClicked<HoverButton> cl) {
		this();
		setClicked(cl);
		setSrc(rurl);
	}

	@Override
	public void createContent() throws Exception {
		Dimension dimension = calculateSize();
		if(null == dimension)
			throw new IllegalStateException("Cannot detect the image dimension of image " + getBackgroundImage());
		int totalWidth = dimension.getWidth();
		int width = totalWidth / 3;
		if(width * 3 != totalWidth || width < dimension.getHeight() - 4)
			System.err.println("ERROR: Resource " + getBackgroundImage() + " for a hover button must have 3 images in width: 1x normal, 1x hover, 1x disabled; it seems wrong");

		setWidth(width + "px");
		setHeight(dimension.getHeight() + "px");
		addCssClass("ui-hvrbtn ui-hvrbtn-" + width);
	}

	/**
	 * Set a new image using a web resource's absolute path. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public void setSrc(String src) {
		m_url = src;
		setBackgroundImage(src);
		forceRebuild();
	}

	@Nullable
	private Dimension calculateSize() throws Exception {
		return DomApplication.get().getResourceInfoCache().getImageDimension(this, getBackgroundImage());
	}
}
