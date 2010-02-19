package to.etc.domui.caches.images;

import java.io.*;

/**
 * The result of a cache lookup where the resulting image has no memory buffers,
 * i.e. it can be accessed by stream or file only.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 10, 2009
 */
class FileImageSource implements IImageStreamSource, IImageFileSource {
	private CachedImageData m_cid;

	FileImageSource(CachedImageData cid) {
		m_cid = cid;
	}

	CachedImageData getCachedImageData() {
		return m_cid;
	}

	@Override
	public File getImageFile() throws Exception {
		return m_cid.getFile();
	}

	@Override
	public InputStream getImageStream() throws Exception {
		return new FileInputStream(getImageFile());
	}

	@Override
	public int getSize() {
		return m_cid.getSize();
	}
}
