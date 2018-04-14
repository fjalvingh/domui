package to.etc.domui.component.image;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.caches.images.FullImage;
import to.etc.domui.util.images.machines.OriginalImagePage;

import java.io.InputStream;

final public class CachedImageInstance implements IUIImageInstance {
	@NonNull
	private final FullImage m_image;

	public CachedImageInstance(@NonNull FullImage image) {
		m_image = image;
	}

	@Override
	public InputStream getImage() throws Exception {
		return m_image.getSource().getImageStream();
	}

	@Override
	public Dimension getDimension() throws Exception {
		OriginalImagePage page = m_image.getInfo().getPageList().get(0);
		return new Dimension(page.getWidth(), page.getHeight());
	}

	@Override
	public int getImageSize() {
		return -1;
	}

	@Override
	public String getMimeType() {
		OriginalImagePage page = m_image.getInfo().getPageList().get(0);
		return page.getMimeType();
	}
}
