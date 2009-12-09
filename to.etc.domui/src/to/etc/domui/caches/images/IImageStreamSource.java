package to.etc.domui.caches.images;

import java.io.*;

/**
 * The most basic of interfaces to access an image's data stream: this allows
 * you to create a datastream to access the image. The stream needs to be closed
 * after use.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 1, 2009
 */
public interface IImageStreamSource {
	/**
	 * Return a datastream for the image's raw data.
	 * @return
	 * @throws Exception
	 */
	InputStream getImageStream() throws Exception;
}
