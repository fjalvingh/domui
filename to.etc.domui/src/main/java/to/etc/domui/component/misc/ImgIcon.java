package to.etc.domui.component.misc;

import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.Span;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-11-18.
 */
final public class ImgIcon extends Span {
	private final Img m_img = new Img();

	public ImgIcon() {
	}

	public ImgIcon(ImageIconRef icon) {
		setIcon(icon);
	}

	public ImgIcon(String src) {
		m_img.setSrc(src);
	}

	@Override public void createContent() throws Exception {
		addCssClass("ui-imgi");
		add(m_img);
		m_img.setImgBorder(0);
	}

	public String getSrc() {
		return m_img.getSrc();
	}

	public void setSrc(String src) {
		m_img.setSrc(src);
	}

	@Override
	public ImgIcon css(String... classes) {
		super.css(classes);
		return this;
	}

	public void setIcon(ImageIconRef icon) {
		setSrc(null == icon ? null : icon.getPath());
	}
}
