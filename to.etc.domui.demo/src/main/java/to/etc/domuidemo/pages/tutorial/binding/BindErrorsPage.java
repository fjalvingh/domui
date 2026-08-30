package to.etc.domuidemo.pages.tutorial.binding;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

import java.math.BigDecimal;

/**
 * Tutorial, "data binding", step 4: input that does not convert never reaches
 * the model, and bindErrors() is what puts those errors on the screen.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class BindErrorsPage extends UrlPage {
	private final AlbumOrder m_order = new AlbumOrder();

	@Override
	public void createContent() throws Exception {
		setPageTitle("Binding errors");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Binding errors"));

		Text2<String> customer = new Text2<>(String.class);
		Text2<Integer> copies = new Text2<>(Integer.class);
		Text2<BigDecimal> price = new Text2<>(BigDecimal.class);

		customer.setMandatory(true);
		customer.bind().to(m_order, AlbumOrder_.customerName());
		copies.bind().to(m_order, AlbumOrder_.copies());
		price.bind().to(m_order, AlbumOrder_.price());

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Customer").mandatory().control(customer);
		fb.label("Copies").control(copies);
		fb.label("Price each").control(price);

		Div result = new Div("dm-tut");
		cp.add(new DefaultButton("Save", a -> {
			if(bindErrors()) {                        // Anything wrong anywhere below this node?
				return;                               // Yes: it is on screen now, stop here.
			}
			result.removeAllChildren();
			result.add("Saved: " + m_order.getCopies() + " copies for " + m_order.getCustomerName()
				+ " at " + m_order.getPrice() + " each");
		}));
		cp.add(result);
		result.add("Fill the form in and press Save. Try \"abc\" in Copies first.");
	}
}
