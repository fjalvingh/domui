package to.etc.domui.component.misc;

import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeBase;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
final public class ImageIcon implements IIcon {
	private final String m_path;

	public ImageIcon(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}

	@Override public NodeBase createNode() {
		String icon = getPath();
		Img img = new Img(icon);
		img.setImgBorder(0);
		return img;
	}
}
