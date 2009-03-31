package to.etc.domui.dom.html;

import to.etc.domui.state.*;
import to.etc.domui.util.*;

/**
 * The base tag for an Image.
 * 
 * <h2>Tips and tricks</h2>
 * <p>To prevent the butt-ugly border on any image placed in an A tag add the following rule to the CSS:
 *  <pre>
 *  a img {
 *      border: medium none;
 *  }
 *  </pre>
 * </p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 4, 2008
 */
public class Img extends NodeBase {
	private String		m_alt;
	private String		m_src;
	private ImgAlign	m_align;
	private int			m_imgBorder = -1;
	private int			m_imgHeight = -1;
	private int			m_imgWidth = -1;

	/**
	 * Creates an uninitialized image.
	 */
	public Img() {
		super("img");
	}

	/**
	 * Creates an image with the specified source. This should be an absolute web resource path.  If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public Img(String src) {
		this();
		setSrc(src);
		setImgBorder(0);
	}
	/**
	 * Creates an image with the specified source. This should be an absolute web resource path.  If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public Img(Class<?> base, String src) {
		this();
		setSrc(base, src);
		setImgBorder(0);
	}

//	/**
//	 * Creates an image with the specified source. This can be theme-relative or it can be an
//	 * absolute web resource path.
//	 * @param themesrc
//	 * @param src
//	 */
//	public Img(boolean themesrc, String src) {
//		this();
//		if(themesrc)
//			setThemeSrc(src);
//		else
//			setSrc(src);
//		setImgBorder(0);
//	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitImg(this);
	}

	/**
	 * Return the current "alt" string of the image.
	 * @return
	 */
	public String getAlt() {
		return m_alt;
	}

	/**
	 * Set the "alt" string of the image.
	 * @param alt
	 */
	public void setAlt(String alt) {
		if(! DomUtil.isEqual(alt, m_alt))
			changed();
		m_alt = alt;
	}

	/**
	 * Get the current source for the image as an absolute web app path.
	 * @return
	 */
	public String getSrc() {
		return PageContext.getRequestContext().translateResourceName(m_src);
	}

	/**
	 * Set the source for this image as an absolute web app path. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param src
	 */
	public void setSrc(String src) {
		if(src.startsWith("/"))
			src = src.substring(1);
//		src = PageContext.getRequestContext().translateResourceName(src);
		if(! DomUtil.isEqual(src, m_src))
			changed();
		m_src = src;
	}

	/**
	 * Set the source as a Java resource based off the given class.
	 * @param base
	 * @param resurl
	 */
	public void	setSrc(Class<?> base, String resurl) {
		String s = DomUtil.getJavaResourceRURL(base, resurl);
		setSrc(s);
	}

//	/**
//	 * Set the image source to come from the current theme.
//	 * @param src
//	 */
//	public void setThemeSrc(String src) {
//		String nw = PageContext.getRequestContext().getRelativeThemePath(src);
//		if(! DomUtil.isEqual(nw, m_src))
//			changed();
//		m_src = nw;
//	}

	public ImgAlign getAlign() {
		return m_align;
	}

	public void setAlign(ImgAlign align) {
		if(m_align != align)
			changed();
		m_align = align;
	}

	public int getImgBorder() {
		return m_imgBorder;
	}

	public void setImgBorder(int imgBorder) {
		if(m_imgBorder != imgBorder)
			changed();
		m_imgBorder = imgBorder;
	}

	public int getImgHeight() {
		return m_imgHeight;
	}

	public void setImgHeight(int imgHeight) {
		if(m_imgHeight != imgHeight)
			changed();
		m_imgHeight = imgHeight;
	}

	public int getImgWidth() {
		return m_imgWidth;
	}

	public void setImgWidth(int imgWidth) {
		if(m_imgWidth != imgWidth)
			changed();
		m_imgWidth = imgWidth;
	}
}
