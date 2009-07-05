package to.etc.domui.component.dynaima;

import java.io.*;

/**
 * UNSTABLE INTERFACE
 * Represents an image source for an image that is present
 * as an encoded byte stream, like present on a file system
 * or present in a database BLOB.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public interface IStreamingImageSource {
	/**
	 * This MUST return the stream's mime type, which must be one of the supported formats (jpeg, gif, png)
	 * @return
	 */
	public String getMimeType();

	/**
	 * Return the size in bytes of the stream. If the size is unknown return -1.
	 * @return
	 */
	public int getSize() throws Exception;

	/**
	 * This must return the stream to use for this resource.
	 * @return
	 * @throws Exception
	 */
	public InputStream getInputStream() throws Exception;

	/**
	 * This will be called when resources are to be released.
	 */
	public void close();
}
