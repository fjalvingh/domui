package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.input.DateInput2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.MessageLine;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.UrlPage;

import java.util.Date;

public class DemoDateInput extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div d = new ContentPanel();
		add(d);

		d.add(new HTag(2, "Date-only input field"));
		DateInput2 di = new DateInput2();
		d.add(di);

		d.add(new VerticalSpacer(40));
		d.add(new HTag(2, "Date and time"));
		DateInput2 dti = new DateInput2(true);
		d.add(dti);
		Div res1 = new Div();
		dti.setOnValueChanged((IValueChanged<DateInput2>) component -> res1.add(new MessageLine(MsgType.INFO, "Selected date and time: " + dti.getValue())));
		d.add(res1);

		d.add(new VerticalSpacer(40));
		d.add(new HTag(2, "Disabled control"));
		DateInput2 dis = new DateInput2(true);
		dis.setDisabled(true);
		dis.setValue(new Date());
		d.add(dis);
	}
}
