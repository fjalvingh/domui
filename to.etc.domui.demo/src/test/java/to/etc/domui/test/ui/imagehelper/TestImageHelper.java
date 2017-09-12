package to.etc.domui.test.ui.imagehelper;

import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-9-17.
 */
public class TestImageHelper {
	@Test
	public void testUnborderedInput() throws Exception {
		String baseName = "/tmp/" + getClass().getSimpleName() + "_testUnborderedInput";

		BufferedImage srcBi;
		try(InputStream is = getClass().getResourceAsStream("input-1.png")) {
			srcBi = ImageIO.read(is);
		}
		ByteImage bi = ByteImage.create(srcBi);
		ByteImage borderBi = bi.stripBorder();

		int[] bl = borderBi.findFontBaselines();

		int sy = borderBi.getRootLocation().y + bl[0];
		int ey = borderBi.getRootLocation().y + bl[1];

		BufferedImage outBi = borderBi.save();
		ImageIO.write(outBi, "png", new File(baseName + "-bw.png"));

		Graphics2D graphics = (Graphics2D) srcBi.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, ey, srcBi.getWidth()-1, ey);
		graphics.dispose();

		ImageIO.write(srcBi, "png", new File(baseName + "-baseline.png"));
		Assert.assertEquals("Baseline should be correct", 11, ey);

	}

	@Test
	public void testBorderedInput() throws Exception {
		String baseName = "/tmp/" + getClass().getSimpleName() + "_testBorderedInput";

		BufferedImage srcBi;
		try(InputStream is = getClass().getResourceAsStream("input-2.png")) {
			srcBi = ImageIO.read(is);
		}
		ByteImage bi = ByteImage.create(srcBi);
		ByteImage borderBi = bi.stripBorder();

		int[] bl = borderBi.findFontBaselines();

		int sy = borderBi.getRootLocation().y + bl[0];
		int ey = borderBi.getRootLocation().y + bl[1];

		BufferedImage outBi = borderBi.save();
		ImageIO.write(outBi, "png", new File(baseName + "-bw.png"));

		Graphics2D graphics = (Graphics2D) srcBi.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, ey, srcBi.getWidth()-1, ey);
		graphics.dispose();

		ImageIO.write(srcBi, "png", new File(baseName + "-baseline.png"));
		Assert.assertEquals("Baseline should be correct", 13, ey);
	}




}
