package to.etc.domui.caches.images;

import javax.annotation.concurrent.*;

import to.etc.domui.caches.filecache.*;
import to.etc.domui.util.images.machines.*;

@Immutable
public class CachedImageInfo extends CachedImageFragment {
	private ImageInfo m_imageData;

	public CachedImageInfo(ImageRoot root, String perm, long sourceVersionLong, FileCacheRef ref, ImageInfo oid, int memload) {
		super(root, perm, sourceVersionLong, memload, ref);
		m_imageData = oid;
	}

	public ImageInfo getImageInfo() {
		return m_imageData;
	}
}
