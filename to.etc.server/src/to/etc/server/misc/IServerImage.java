package to.etc.server.misc;

/**
 * This encapsulates some server-generated image. It has methods to access
 * basic info pertaining to the image. The image is returned as a 
 * server-side URL.
 * 
 * <p>Created on May 30, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface IServerImage {
	public int getWidth();

	public int getHeight();

	public String getURL();
}
