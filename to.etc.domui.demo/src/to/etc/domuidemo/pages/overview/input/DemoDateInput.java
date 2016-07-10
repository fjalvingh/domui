package to.etc.domuidemo.pages.overview.input;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoDateInput extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div d = new Div();
		add(d);

		d.add("Date");
		DateInput di = new DateInput();
		d.add(di);

		add(new VerticalSpacer(40));
		final DateInput dti = new DateInput(true);
		d	= new Div();
		add(d);
		d.add("Date and time");
		d.add(dti);
		dti.setOnValueChanged(new IValueChanged<DateInput>() {
			@Override
			public void onValueChanged(DateInput component) throws Exception {
				addChange(dti.getValue());
			}
		});


		add(new VerticalSpacer(40));
		d = new Div();
		add(d);
		d.add("Disabled control");
		final DateInput dis = new DateInput(true);
		dis.setDisabled(true);
		dis.setValue(new Date());
		d.add(dis);

		add(new VerticalSpacer(40));
		d = new Div();
		add(d);
		d.add("");
		DateInput ccc = new DateInput();
		d.add(ccc);
		ccc.setOnValueChanged(new IValueChanged<DateInput>() {
			@Override
			public void onValueChanged(DateInput component) throws Exception {
				addChange(component.getValue());
			}
		});
	}

	protected void addChange(Date value) {
		add(new MsgDiv("Selected is: " + value));
	}
}
