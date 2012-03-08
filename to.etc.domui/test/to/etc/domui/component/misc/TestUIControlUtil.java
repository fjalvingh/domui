package to.etc.domui.component.misc;

import java.awt.*;

import org.junit.*;

public class TestUIControlUtil {
	@Test
	public void testGetRgbHex() {
		String red = UIControlUtil.getRgbHex(Color.red, true);
		Assert.assertEquals("#ff0000", red);
		String green = UIControlUtil.getRgbHex(Color.green, true);
		Assert.assertEquals("#00ff00", green);
		String blue = UIControlUtil.getRgbHex(Color.blue, true);
		Assert.assertEquals("#0000ff", blue);
	}
}
