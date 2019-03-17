package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.input.Text2;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.UrlPage;

import java.math.BigDecimal;

public class DemoText extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);

		Label l = new Label("String");
		d.add(l);
		Text2<String> ts = new Text2<String>(String.class);
		d.add(ts);
		ts.setValue("Example");

		d.add(new BR());
		d.add(new BR());
		l = new Label("Integer");
		d.add(l);
		Text2<Integer> ti = new Text2<Integer>(Integer.class);
		d.add(ti);
		ti.setValue(143);

		d.add(new BR());
		d.add(new BR());
		l = new Label("Long");
		d.add(l);
		Text2<Long> tl = new Text2<Long>(Long.class);
		d.add(tl);
		tl.setSize(8);
		tl.setMaxLength(16);
		tl.setValue(1826742L);

		d.add(new BR());
		d.add(new BR());
		l = new Label("Double");
		d.add(l);
		Text2<Double> td = new Text2<Double>(Double.class);
		d.add(td);
		td.setSize(14);
		td.setMaxLength(16);
		//-- using rawvalue
		td.setValue(2143.34d);

		d.add(new BR());
		d.add(new BR());
		l = new Label("Float");
		d.add(l);
		Text2<Float> tf = new Text2<Float>(Float.class);
		d.add(tf);
		tf.setSize(14);
		tf.setMaxLength(16);
		tf.setValue(98234.11F);

		d.add(new BR());
		d.add(new BR());
		l = new Label("BigDecimal");
		d.add(l);

		Text2<BigDecimal> tbd = new Text2<BigDecimal>(BigDecimal.class);
		d.add(tbd);
		tbd.setSize(25);
		tbd.setMaxLength(32);
		tbd.setValue(new BigDecimal("8192.76"));
	}
}

