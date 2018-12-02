package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.dom.html.RadioGroup;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-12-18.
 */
public class RadioButtonPage extends UrlPage {
	@Override public void createContent() throws Exception {
		RadioGroup<TestEnum> rg = RadioGroup.createFromEnum(TestEnum.class);
		add(rg);
		rg.addCssClass("ui-rbb-buttons");


	}
}
