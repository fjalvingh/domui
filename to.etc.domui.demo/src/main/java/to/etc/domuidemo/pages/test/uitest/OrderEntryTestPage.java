package to.etc.domuidemo.pages.test.uitest;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleListModel;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.css.TextAlign;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.converter.MoneyBigDecimalNoSign;
import to.etc.domuidemo.pages.tutorial.tables.BasketLine;
import to.etc.domuidemo.pages.tutorial.tables.BasketLine_;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * The fixture page the testing documentation is written around: a small order
 * form, a basket as a DataTable, and an answer that changes when something is
 * ordered. Everything on it is fixed data, so a test always sees the same
 * screen.
 *
 * The tests in the .test package below this one drive this page - one of them
 * with the page object generated from it - and the "Testing" section of the
 * documentation quotes both.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class OrderEntryTestPage extends UrlPage {
	static private final String NOTHING = "Nothing ordered yet";

	public enum Shipping {
		Standard, Express, Pickup
	}

	@Override
	public void createContent() throws Exception {
		setPageTitle("Order entry");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Order entry"));

		Div answer = new Div("dm-tut", NOTHING);
		answer.setTestID("answer");

		Text2<String> customer = new Text2<>(String.class);
		customer.setMandatory(true);
		ComboFixed2<Shipping> shipping = ComboFixed2.createEnumCombo(Shipping.class);
		shipping.setValue(Shipping.Standard);
		Text2<Integer> copies = new Text2<>(Integer.class);
		copies.setValue(1);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Customer").control(customer);
		fb.label("Shipping").control(shipping);
		fb.label("Copies").control(copies);

		List<BasketLine> basket = List.of(
			new BasketLine("Rubber Soul", 1, new BigDecimal("14.95")),
			new BasketLine("Revolver", 1, new BigDecimal("12.50")),
			new BasketLine("Abbey Road", 1, new BigDecimal("19.95"))
		);

		RowRenderer<BasketLine> rr = new RowRenderer<>(BasketLine.class);
		rr.column(BasketLine_.title()).label("Album").width(30);
		rr.column(BasketLine_.price()).label("Price each").width(15).align(TextAlign.RIGHT)
			.converter(new MoneyBigDecimalNoSign());
		rr.column().label("Order").width(10)
			.renderer((node, line) -> node.add(new LinkButton("Order", ()-> order(answer, customer, shipping, copies, line))));

		DataTable<BasketLine> table = new DataTable<>(new SimpleListModel<>(basket), rr);
		table.setTestID("basket");
		cp.add(table);

		cp.add(new DefaultButton("Clear", ()-> {
			answer.removeAllChildren();
			answer.add(NOTHING);
		}));
		cp.add(answer);
	}

	/**
	 * Ordering reads the three controls. The customer field is mandatory, so
	 * when it is empty its getValue() posts the error and ends this handler:
	 * the answer stays as it was.
	 */
	private void order(Div answer, Text2<String> customer, ComboFixed2<Shipping> shipping, Text2<Integer> copies, BasketLine line) {
		String who = customer.getValue();
		Integer count = copies.getValue();
		Shipping how = shipping.getValue();

		BigDecimal total = line.getPrice()
			.multiply(new BigDecimal(null == count ? 0 : count.intValue()))
			.add(shippingCost(how))
			.setScale(2, RoundingMode.HALF_UP);

		answer.removeAllChildren();
		answer.add(who + " ordered " + count + " x " + line.getTitle() + ", " + how + ", total " + total);
	}

	static private BigDecimal shippingCost(Shipping how) {
		switch(how) {
			default:
				return BigDecimal.ZERO;
			case Standard:
				return new BigDecimal("3.95");
			case Express:
				return new BigDecimal("9.95");
		}
	}
}
