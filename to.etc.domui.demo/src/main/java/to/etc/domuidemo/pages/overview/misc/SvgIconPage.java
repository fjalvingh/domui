package to.etc.domuidemo.pages.overview.misc;

import to.etc.domui.component.misc.SvgIcon;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-11-18.
 */
public class SvgIconPage extends UrlPage {
	@Override public void createContent() throws Exception {
		add(new HTag(1, "SVG Icon raw"));

		add(new SvgIcon("img/lion.svg"));



	}
}
