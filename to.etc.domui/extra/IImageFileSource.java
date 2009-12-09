package to.etc.domui.util.images.cache;

import java.io.*;

/**
 * An image source which is backed by some file. This one is the most expensive source
 * in many ways because it often forces cached data to a file. Only use when absolutely
 * necessary.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 1, 2009
 */
public interface IImageFileSource extends IImageStreamSource {
	File getImageFile() throws Exception;
}
