package to.etc.domuidemo.pages.spi;

import to.etc.domui.annotations.UIPage;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.misc.Explanation;
import to.etc.domui.dom.html.SubPage;
import to.etc.domui.state.UIGoto;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-20.
 */
@UIPage("/welcome")
public class WelcomeSpi extends SubPage {
	@Override public void createContent() throws Exception {
		add(new Explanation("Initial spi page"));
		add(new DefaultButton("Click here", a -> {
			UIGoto.moveSub(SpiTarget.Main, TestDataSpi.class);
		}));
	}
}
