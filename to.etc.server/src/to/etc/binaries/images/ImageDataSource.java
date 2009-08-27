package to.etc.binaries.images;

import to.etc.binaries.cache.*;

/**
 * Represents a (temp) datasource used when manipulating binaries. This
 * wraps the information known about the image with accessors to it's
 * data. The preferred way to access an image is to use the Stream calls; only
 * if you are not able to use streams should you use the File calls as these
 * might cause a tempfile to be created.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 15, 2006
 */
public interface ImageDataSource extends BinaryDataSource, ImageInfo {
	public int getWidth();

	public int getHeight();

	public String getMime();

	public int getSize();

}
