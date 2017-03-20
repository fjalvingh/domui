package to.etc.domuidemo.pages.overview.misc;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoDisplayCheckbox extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);

		Label l = new Label("DisplayCheckbox checked");
		d.add(l);
		DisplayCheckbox dcc = new DisplayCheckbox();
		dcc.setValue(Boolean.TRUE);
		d.add(dcc);

		l = new Label(" and unchecked");
		d.add(l);
		DisplayCheckbox dcu = new DisplayCheckbox();
		dcu.setValue(Boolean.FALSE);
		d.add(dcu);
	}
}
