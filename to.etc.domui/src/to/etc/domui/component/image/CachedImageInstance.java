package to.etc.domui.component.image;

import java.io.*;

import javax.annotation.*;

import to.etc.domui.caches.images.*;
import to.etc.domui.util.images.machines.*;

final public class CachedImageInstance implements IUIImageInstance {
	@Nonnull
	private final FullImage m_image;

	public CachedImageInstance(@Nonnull FullImage image) {
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
