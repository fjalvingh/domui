package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.buttons.CheckboxButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

public class DemoCheckbox extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add(new HTag(1, "CheckboxButton sizes"));
		FormBuilder fb = new FormBuilder(cp);

		fb.label("Normal size").control(new CheckboxButton());
		fb.label("Small").control(new CheckboxButton().css("is-small"));
		fb.label("Medium").control(new CheckboxButton().css("is-medium"));
		fb.label("Large").control(new CheckboxButton().css("is-large"));
		fb.label("X-Large").control(new CheckboxButton().css("is-xlarge"));
	}
}
