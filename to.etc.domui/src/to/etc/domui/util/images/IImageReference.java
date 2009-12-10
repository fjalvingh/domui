package to.etc.domui.util.images;

import java.io.*;

import javax.annotation.*;

/**
 * This is a reference to some individual original image as returned by
 * IImageRetriever. It is a must-close resource.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 30, 2009
 */
public interface IImageReference extends Closeable {
	@Override
	public void close() throws IOException;

	/**
	 * If this retriever accesses resources that can change after use this must return some usable
	 * indication of the version, usually a "last date changed" timestamp. This value should remain
	 * unchanged over invocations if the object accessed has not changed. It should return -1 if
	 * the source object has been deleted; it should return 0 if the timestamp does not matter.
	 * This gets called multiple times; it should be fast.
	 *
	 * @return
	 */
	public long getVersionLong() throws Exception;

	/**
	 * This must return the image's actual mime type.
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public String getMimeType() throws Exception;

	/**
	 * Returns the datastream containing this image. This may be called only ONCE for an image and must
	 * be closed after use.
	 * @return
	 * @throws Exception
	 */
	public InputStream getInputStream() throws Exception;

}
