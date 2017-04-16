package to.etc.domuidemo.pages.overview.input;

import java.math.*;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;

public class DemoText extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);

		Label l = new Label("String");
		d.add(l);
		Text<String> ts = new Text<String>(String.class);
		d.add(ts);
		ts.setValue("Example");

		d.add(new BR());
		d.add(new BR());
		l = new Label("Integer");
		d.add(l);
		Text<Integer> ti = new Text<Integer>(Integer.class);
		d.add(ti);
		ti.setValue(143);

		d.add(new BR());
		d.add(new BR());
		l = new Label("Long");
		d.add(l);
		Text<Long> tl = new Text<Long>(Long.class);
		d.add(tl);
		tl.setSize(8);
		tl.setMaxLength(16);
		tl.setValue(1826742L);

		d.add(new BR());
		d.add(new BR());
		l = new Label("Double");
		d.add(l);
		Text<Double> td = new Text<Double>(Double.class);
		d.add(td);
		td.setSize(14);
		td.setMaxLength(16);
		//-- using rawvalue
		td.setRawValue("2143.34");

		d.add(new BR());
		d.add(new BR());
		l = new Label("Float");
		d.add(l);
		Text<Float> tf = new Text<Float>(Float.class);
		d.add(tf);
		tf.setSize(14);
		tf.setMaxLength(16);
		tf.setValue(98234.11F);

		d.add(new BR());
		d.add(new BR());
		l = new Label("BigDecimal");
		d.add(l);
		Text<BigDecimal> tbd = new Text<BigDecimal>(BigDecimal.class);
		d.add(tbd);
		tbd.setSize(25);
		tbd.setMaxLength(32);
		//-- using rawvalue
		tbd.setRawValue("5294987394387");
	}
}

