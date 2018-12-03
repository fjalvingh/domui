package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.RadioGroup;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-12-18.
 */
public class RadioButtonPage extends UrlPage {
	@Override public void createContent() throws Exception {
		RadioGroup<TestEnum> rg = RadioGroup.createFromEnum(TestEnum.class);
		rg.addCssClass("ui-rbb-buttons");

		ContentPanel cp = new ContentPanel();
		add(cp);
		FormBuilder fb = new FormBuilder(cp);
		fb.label("Enabled").item(rg);

		rg = RadioGroup.createFromEnum(TestEnum.class);
		rg.addCssClass("ui-rbb-buttons");
		rg.setDisabled(true);
		fb.label("Disabled").item(rg);

		rg = RadioGroup.createFromEnum(TestEnum.class);
		fb.label("Enabled").item(rg);

		rg = RadioGroup.createFromEnum(TestEnum.class);
		rg.setDisabled(true);
		fb.label("Disabled").item(rg);


	}
}
