package to.etc.domui.caches.images;

/**
 * An image retrieval result which encapsulates an image that was
 * present in memory, i.e. one that has bufferset available.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 10, 2009
 */
final class MemoryImageSource extends FileImageSource implements IImageMemorySource {
	public MemoryImageSource(CachedImageData cid) {
		super(cid);
	}

	@Override
	public byte[][] getImageBuffers() throws Exception {
		return getCachedImageData().getBuffers();
	}
}
