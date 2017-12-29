package to.etc.domui.component.misc;

import org.junit.*;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;

/**
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 18-8-17.
 */
public class TestTabInstance {

	@Test
	public void setImage_emptyString_expectNoImage() throws Exception {
		// Setup
		TabPanelBase tabPanelBase = new TabPanel(false);

		// Execute
		tabPanelBase.add(new Div("Some content"), "Content title", "");

		// Verify
		TabInstance tabInstance = tabPanelBase.getCurrentTabInstance();
		Assert.assertNull(tabInstance.getImage());
	}

	@Test
	public void setImage_noEmptyString_expectImage() throws Exception {
		// Setup
		TabPanelBase tabPanelBase = new TabPanel(false);

		// Execute
		tabPanelBase.add(new Div("Some content"), "Content title", "iconString");

		// Verify
		TabInstance tabInstance = tabPanelBase.getCurrentTabInstance();
		Assert.assertNotNull(tabInstance.getImage());
	}
}
