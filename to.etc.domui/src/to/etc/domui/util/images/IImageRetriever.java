package to.etc.domui.util.images;

import javax.annotation.*;

/**
 * Factory to obtain an image's data from a per-retriever key string.
 *
 * UNSTABLE INTERFACE
 * Thingy which can obtain images from some source (signal interface).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public interface IImageRetriever {
	/**
	 * Returns an unique string identifier, usable in file names and URL's, to represent all images
	 * obtained from this retriever. Called once when the retriever is registered. The value returned
	 * by this call may not change over the lifetime of this factory.
	 * @return
	 */
	@Nonnull
	public String getRetrieverKey();

	/**
	 * When this returns true the datastream obtained through here should be cached on the file system.
	 * @return
	 */
	public boolean cacheOnFS();

	/**
	 * Returns the check interval, in millis. This is the age that an image may have in the cache before it's
	 * rechecked for changes again. Returning 0 means the image gets checked for validity always.
	 * @return
	 */
	public long getCheckInterval();

	public IImageReference loadImage(String key) throws Exception;
}
