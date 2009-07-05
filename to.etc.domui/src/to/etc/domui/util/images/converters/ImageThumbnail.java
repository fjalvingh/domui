package to.etc.domui.util.images.converters;

/**
 * A specialization indicating that the target conversion is a thumbnail instead of
 * a normal smaller image.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 8, 2008
 */
public class ImageThumbnail extends ImageResize {
	public ImageThumbnail(int height, int width, int filterspec, String targetMime) {
		super(height, width, filterspec, targetMime);
	}

	public ImageThumbnail(int height, int width, String targetMime) {
		super(height, width, targetMime);
	}

	public ImageThumbnail(int height, int width, int filterspec) {
		super(height, width, filterspec);
	}

	public ImageThumbnail(int height, int width) {
		super(height, width);
	}

}
