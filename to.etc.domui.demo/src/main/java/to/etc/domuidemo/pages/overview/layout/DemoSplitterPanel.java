package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domuidemo.pages.*;

public class DemoSplitterPanel extends UrlPage {
	@Override
	public void createContent() throws Exception {

		Div d = new Div();
		add(d);
		d.setWidth("90%");
		d.setHeight("400px");
		d.setBorder(1, "grey", "solid");

		Div one = createPanel(); // Lorem panel 1
		Div two = createPanel(); // And the second one
		SplitterPanel sp = new SplitterPanel(one, two, true);
		d.add(sp);
	}

	private Div createPanel() {
		Div d = new Div();
		d.setWidth("400px");
		for(int i = 0; i < 3; i++) {
			Div dd = new Div();
			d.add(dd);
			dd.setText(Lorem.getPara());
			dd.setMarginBottom("10px");
		}

		return d;
	}
}
