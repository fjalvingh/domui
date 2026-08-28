package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.buttons.CheckboxButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.UrlPage;

public class DemoCheckbox extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		Checkbox cb = new Checkbox();
		cp.add(cb);
		cp.add(" Simple checkbox");

		cp.add(new VerticalSpacer(40));

		cb = new Checkbox();
		cp.add(cb);
		cp.add(" Simple checkbox with change listener");
		cb.setOnValueChanged(new IValueChanged<Checkbox>() {
			@Override
			public void onValueChanged(Checkbox component) throws Exception {
				Div d = new Div();
				d.setText("Value changed to " + component.getValue());
				DemoCheckbox.this.add(d);
			}
		});


		cp.add(new HTag(2, "Checkbox Buttons"));
		FormBuilder fb = new FormBuilder(cp);

		fb.label("Normal size").control(new CheckboxButton());
		fb.label("Small").control(new CheckboxButton().css("is-small"));
		fb.label("Medium").control(new CheckboxButton().css("is-medium"));
		fb.label("Large").control(new CheckboxButton().css("is-large"));
		fb.label("X-Large").control(new CheckboxButton().css("is-xlarge"));
	}
}
