package to.etc.domuidemo.pages.tutorial.components;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

import java.math.BigDecimal;

/**
 * Tutorial, "using components", step 3: setOnValueChanged - the control
 * tells the server that its value changed, as soon as the user leaves it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ComponentChangePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Reacting to a changed value");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Reacting to a changed value"));

		Text2<Integer> copies = new Text2<>(Integer.class);
		copies.setValue(1);

		Text2<BigDecimal> price = new Text2<>(BigDecimal.class);
		price.setValue(new BigDecimal("14.95"));

		Div total = new Div("dm-tut");

		copies.setOnValueChanged(c -> showTotal(copies, price, total));
		price.setOnValueChanged(c -> showTotal(copies, price, total));

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Copies").control(copies);
		fb.label("Price each").control(price);

		cp.add(total);
		showTotal(copies, price, total);
	}

	private void showTotal(Text2<Integer> copies, Text2<BigDecimal> price, Div total) {
		Integer copiesValue = copies.getValueSafe();
		BigDecimal priceValue = price.getValueSafe();

		total.removeAllChildren();
		if(copiesValue == null || priceValue == null) {
			total.add("Fill in both fields to see the total");
		} else {
			total.add("Total: " + priceValue.multiply(new BigDecimal(copiesValue)));
		}
	}
}
