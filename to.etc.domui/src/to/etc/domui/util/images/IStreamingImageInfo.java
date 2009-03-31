package to.etc.domui.util.images;

import java.io.*;

import to.etc.domui.util.images.converters.*;

/**
 * This gets returned by the streaming image factory classes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public interface IStreamingImageInfo {
	public String			getMimeType() throws Exception;
	
	/**
	 * This returns the image's data if that data is available. If not this returns null.
	 * @return
	 * @throws Exception
	 */
	public ImageData		getImageData() throws Exception;

	/**
	 * Returns the datastream containing this image. This may be called only ONCE for an image and must
	 * be closed after use.
	 * @return
	 * @throws Exception
	 */
	public InputStream		getInputStream() throws Exception;
}
