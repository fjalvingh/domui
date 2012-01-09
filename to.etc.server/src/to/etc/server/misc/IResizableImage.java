package to.etc.server.misc;

import java.awt.*;

/**
 * This interface is for objects that provide a single server-generated
 * image.
 * <p>Created on May 30, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface IResizableImage extends ImageDescriptor {
	public IFixedSizeImage getImage(int width, int height) throws Exception;


	/** The image's mime type */
	public String getMimeType();

	/** Returns the original size of this image, in pixels; returns null for media types like pdf and svg. */
	public Dimension getSize();
}
