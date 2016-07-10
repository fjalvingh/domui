package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoCheckbox extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Checkbox cb = new Checkbox();
		add(cb);
		add(" Simple checkbox");

		add(new VerticalSpacer(40));

		cb = new Checkbox();
		add(cb);
		add(" Simple checkbox with change listener");
		cb.setOnValueChanged(new IValueChanged<Checkbox>() {
			@Override
			public void onValueChanged(Checkbox component) throws Exception {
				Div d = new Div();
				d.setText("Value changed to " + component.getValue());
				DemoCheckbox.this.add(d);
			}
		});
	}

}
