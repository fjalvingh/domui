package to.etc.domui.component.dynaima;

import java.awt.image.*;

/**
 * The source of the image is a BufferedImage.
 * 
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public interface IBufferedImageSource {
	/**
	 * Return or create the bufferedImage to output.
	 * 
	 * @return
	 * @throws Exception
	 */
	public BufferedImage			getImage() throws Exception;

	/**
	 * The output format (mime for jpg, gif, png).
	 * @return
	 */
	public String					getMimeType();
}
