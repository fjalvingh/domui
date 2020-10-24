package to.etc.domuidemo.pages.spi;

import to.etc.domui.component.misc.Explanation;
import to.etc.domui.dom.html.SubPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-20.
 */
public class WelcomeSpi extends SubPage {
	@Override public void createContent() throws Exception {
		add(new Explanation("Initial spi page"));
	}
}
