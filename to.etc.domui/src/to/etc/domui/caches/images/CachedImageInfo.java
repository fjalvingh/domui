package to.etc.domui.caches.images;

import to.etc.domui.caches.filecache.*;
import to.etc.domui.util.images.machines.*;

public class CachedImageInfo extends CachedImageFragment {
	private ImageInfo m_imageData;

	/** The cacheref for the file while this thingy is in use. */
	private FileCacheRef m_fileRef;

	public CachedImageInfo(ImageRoot root, String perm, long sourceVersionLong, FileCacheRef ref, ImageInfo oid, int memload) {
		super(root, perm, sourceVersionLong);
		m_fileRef = ref;
		m_imageData = oid;
		setMemoryCacheSize(memload);
	}

	public ImageInfo getImageInfo() {
		return m_imageData;
	}
}
