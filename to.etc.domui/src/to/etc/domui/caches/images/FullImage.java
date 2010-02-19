package to.etc.domui.caches.images;

import to.etc.domui.util.images.machines.*;

/**
 * Contains both the data source and the image info for a single image instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 10, 2009
 */
final public class FullImage {
	private IImageStreamSource m_source;

	private ImageInfo m_info;

	public FullImage(IImageStreamSource source, ImageInfo info) {
		m_source = source;
		m_info = info;
	}

	public ImageInfo getInfo() {
		return m_info;
	}

	public IImageStreamSource getSource() {
		return m_source;
	}
}
