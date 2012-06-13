package to.etc.domuidemo.pages.overview.misc;

import java.math.*;
import java.util.*;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoDisplayValue extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);

		Label l = new Label("DisplayValue<String>");
		d.add(l);
		DisplayValue<String> dvt = new DisplayValue<String>(String.class);
		dvt.setValue("Tekst is readonly");
		d.add(dvt);

		d.add(new BR());
		l = new Label("DisplayValue<Integer>");
		d.add(l);
		DisplayValue<Integer> dvi = new DisplayValue<Integer>(Integer.class);
		d.add(dvi);
		dvi.setValue(143);
		dvi.setWidth("4em");

		d.add(new BR());
		l = new Label("DisplayValue<Long>");
		d.add(l);
		DisplayValue<Long> dvl = new DisplayValue<Long>(Long.class);
		d.add(dvl);
		dvl.setMaxWidth("16px");
		dvl.setValue(1826742L);

		d.add(new BR());
		l = new Label("DisplayValue<Double>");
		d.add(l);
		DisplayValue<Double> dvd = new DisplayValue<Double>(Double.class);
		d.add(dvd);
		dvd.setMaxWidth("16em");
		dvd.setValue(2143.34D);

		d.add(new BR());
		l = new Label("DisplayValue<Float>");
		d.add(l);
		DisplayValue<Float> dvf = new DisplayValue<Float>(Float.class);
		d.add(dvf);
		dvf.setMaxWidth("16em");
		dvf.setValue(98234.11F);

		d.add(new BR());
		l = new Label("DisplayValue<BigDecimal>");
		d.add(l);
		DisplayValue<BigDecimal> dvbd = new DisplayValue<BigDecimal>(BigDecimal.class);
		d.add(dvbd);
		dvbd.setMaxWidth("32em");
		//-- using rawvalue
		dvbd.setValue(new BigDecimal("1115.37"));

		d.add(new BR());
		l = new Label("DisplayValue<Date>");
		d.add(l);
		DisplayValue<Date> date = new DisplayValue<Date>(Date.class);
		date.setValue(new Date());
		date.setWidth("10em");
		d.add(date);

	}
}
