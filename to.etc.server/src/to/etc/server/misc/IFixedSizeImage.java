package to.etc.server.misc;

import java.awt.*;

/**
 * This encapsulates some server-generated image. It has methods to access
 * basic info pertaining to the image. The image is returned as a 
 * server-side URL.
 * 
 * <p>Created on May 30, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface IFixedSizeImage extends ImageDescriptor {
	public String getURL();

	/** The image's mime type */
	public String getMimeType();

	/** Returns the actual size of this image, in pixels; returns null for media types like pdf and svg. */
	public Dimension getSize();
}
