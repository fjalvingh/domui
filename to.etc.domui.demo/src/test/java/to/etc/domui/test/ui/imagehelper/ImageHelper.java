package to.etc.domui.test.ui.imagehelper;

import javax.imageio.ImageIO;
import java.awt.*;
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

		int[] bl = borderBi.findFontBaselines();

		int sy = borderBi.getRootLocation().y + bl[0];
		int ey = borderBi.getRootLocation().y + bl[1];

		BufferedImage outBi = borderBi.save();
		ImageIO.write(outBi, "png", new File("/tmp/o1.png"));

		Graphics2D graphics = (Graphics2D) srcBi.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, ey, srcBi.getWidth()-1, ey);
		graphics.dispose();

		ImageIO.write(srcBi, "png", new File("/tmp/o2.png"));
	}
}
