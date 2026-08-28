package to.etc.domuidemo.pages.test;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.*;

public class ImageEntitiesTest extends UrlPage {
	private Img m_img = new Img();

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add(m_img);
		m_img.setSrc("to.etc.domui.parts.PropBtnPart.part?src=$themes/blue/defaultbutton.properties&txt=%21Inklappen&icon=$themes%2fblue%2fbtnHideLookup.png");
	}
}
