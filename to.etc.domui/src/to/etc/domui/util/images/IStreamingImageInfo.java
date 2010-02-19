package to.etc.domui.util.images;

import java.io.*;

import to.etc.domui.util.images.machines.*;

/**
 * This gets returned by the streaming image factory classes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public interface IStreamingImageInfo {
	/**
	 * If this retriever accesses resources that can change after use this must return some usable
	 * indication of the version, usually a "last date changed" timestamp. This value should remain
	 * unchanged over invocations if the object accessed has not changed. It should return -1 if
	 * the source object has been deleted; it should return 0 if the timestamp does not matter.
	 *
	 * @return
	 */
	public long getLastModifiedDate(String key) throws Exception;

	public String getMimeType() throws Exception;

	/**
	 * This returns the image's data if that data is available. If not this returns null.
	 * @return
	 * @throws Exception
	 */
	public ImageInfo getImageData() throws Exception;

	/**
	 * Returns the datastream containing this image. This may be called only ONCE for an image and must
	 * be closed after use.
	 * @return
	 * @throws Exception
	 */
	public InputStream getInputStream() throws Exception;
}
