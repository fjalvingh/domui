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
		//String baseName = "/tmp/" + getClass().getSimpleName() + "_testUnborderedInput";

		BufferedImage srcBi;
		try(InputStream is = getClass().getResourceAsStream("input-1.png")) {
			srcBi = ImageIO.read(is);
		}
		ByteImage bi = ByteImage.create(srcBi);
		ByteImage borderBi = bi.stripBorder();

		int[] bl = borderBi.findFontBaselines();

		int sy = borderBi.getRootLocation().y + bl[0];
		int ey = borderBi.getRootLocation().y + bl[1];

		File f1 = File.createTempFile("ubborder-", "-bw.png");
		BufferedImage outBi = borderBi.save();
		ImageIO.write(outBi, "png", f1);

		Graphics2D graphics = (Graphics2D) srcBi.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, ey, srcBi.getWidth()-1, ey);
		graphics.dispose();

		File f2 = File.createTempFile("unborder-", "-baseline.png");
		ImageIO.write(srcBi, "png", f2);
		Assert.assertEquals("Baseline should be correct", 11, ey);
		f1.delete();
		f2.delete();

	}

	@Test
	public void testBorderedInput() throws Exception {
		//String baseName = "/tmp/" + getClass().getSimpleName() + "_testBorderedInput";

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
		File f1 = File.createTempFile("testborder-", "-bw.png");
		ImageIO.write(outBi, "png", f1);

		Graphics2D graphics = (Graphics2D) srcBi.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, ey, srcBi.getWidth()-1, ey);
		graphics.dispose();

		File f2 = File.createTempFile("testborder-", "-baseline.png");
		ImageIO.write(srcBi, "png", f2);
		Assert.assertEquals("Baseline should be correct", 13, ey);
		f1.delete();
		f2.delete();

	}




}
