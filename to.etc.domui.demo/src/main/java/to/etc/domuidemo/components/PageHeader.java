package to.etc.domuidemo.components;

import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.Div;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-8-17.
 */
public class PageHeader extends Div {
	@Override public void createContent() throws Exception {
		add(new SourceBreadCrumb());
		add(new VerticalSpacer(10));
	}
}
