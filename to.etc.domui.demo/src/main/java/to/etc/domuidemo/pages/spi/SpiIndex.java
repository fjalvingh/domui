package to.etc.domuidemo.pages.spi;

import to.etc.domui.annotations.UIPage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.SpiPage;
import to.etc.domuidemo.components.PageHeader;

/**
 * Example root page for a SPI application.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-20.
 */
@UIPage("/spi")
public class SpiIndex extends SpiPage {
	@Override public void createContent() throws Exception {
		add(new PageHeader());
		Div content = new Div();
		add(content);
		registerContainer("c", content, WelcomeSpi.class);
	}
}
