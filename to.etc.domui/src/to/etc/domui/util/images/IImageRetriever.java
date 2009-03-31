package to.etc.domui.util.images;

/**
 * UNSTABLE INTERFACE
 * Thingy which can obtain images from some source (signal interface).
 * 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public interface IImageRetriever {
	public String				keyAsFilenameString(Object s);
	public IStreamingImageInfo	loadImage(Object key) throws Exception;
}
