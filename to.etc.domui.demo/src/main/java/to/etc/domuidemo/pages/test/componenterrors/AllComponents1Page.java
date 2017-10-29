package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 11-9-17.
 */
public class AllComponents1Page extends UrlPage {
	@Override public void createContent() throws Exception {
		add(new TextFragment());
		add(new VerticalSpacer(20));
		add(new Text2Fragment());
		add(new VerticalSpacer(20));
		add(new LookupInputFragment());
		add(new VerticalSpacer(20));
		add(new DateInputFragment());

		add(new VerticalSpacer(800));
	}
}
