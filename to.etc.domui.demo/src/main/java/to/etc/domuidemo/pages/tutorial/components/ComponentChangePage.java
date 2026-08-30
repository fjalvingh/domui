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
	private final Text2<Integer> m_copies = new Text2<>(Integer.class);

	private final Text2<BigDecimal> m_price = new Text2<>(BigDecimal.class);

	private final Div m_total = new Div("dm-tut");

	@Override
	public void createContent() throws Exception {
		setPageTitle("Reacting to a changed value");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Reacting to a changed value"));

		m_copies.setValue(1);
		m_price.setValue(new BigDecimal("14.95"));

		m_copies.setOnValueChanged(c -> showTotal());
		m_price.setOnValueChanged(c -> showTotal());

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Copies").control(m_copies);
		fb.label("Price each").control(m_price);

		cp.add(m_total);
		showTotal();
	}

	private void showTotal() {
		Integer copies = m_copies.getValueSafe();
		BigDecimal price = m_price.getValueSafe();

		m_total.removeAllChildren();
		if(copies == null || price == null) {
			m_total.add("Fill in both fields to see the total");
		} else {
			m_total.add("Total: " + price.multiply(new BigDecimal(copies)));
		}
	}
}
