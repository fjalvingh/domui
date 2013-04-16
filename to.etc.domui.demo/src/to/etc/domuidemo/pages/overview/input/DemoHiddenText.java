package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;

public class DemoHiddenText extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);

		Label ls = new Label("HiddenText<String> Password text input");
		d.add(ls);
		HiddenText<String> hidden1 = new HiddenText<String>(String.class);
		d.add(hidden1);

		d.add(new BR());
		ls = new Label("HiddenText<Long> input numeric only");
		d.add(ls);
		HiddenText<Long> hidden2 = new HiddenText<Long>(Long.class);
		d.add(hidden2);
	}
}
