package to.etc.domui.test.imagehelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-8-17.
 */
public class ImageHelper {








	static public void main(String[] args) throws Exception {
		BufferedImage srcBi = ImageIO.read(new File("/tmp/input-1.png"));
		ByteImage bi = ByteImage.create(srcBi);

		ByteImage borderBi = bi.stripBorder();

		BufferedImage outBi = borderBi.save();
		ImageIO.write(outBi, "png", new File("/tmp/o1.png"));

	}
}
