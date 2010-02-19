package to.etc.domui.caches.images;

/**
 * This is a stream source which allows access to a set of buffers representing the
 * image data. It is often present on image sources that represent cached data. Using
 * this when available can prevent buffering between reader and writer streams.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 1, 2009
 */
public interface IImageMemorySource extends IImageStreamSource {
	byte[][] getImageBuffers() throws Exception;
}
