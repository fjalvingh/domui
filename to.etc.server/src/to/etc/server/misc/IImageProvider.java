package to.etc.server.misc;

/**
 * This interface is for objects that provide a single server-generated
 * image.
 * <p>Created on May 30, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface IImageProvider {
	public IServerImage getImage(int width, int height) throws Exception;
}
