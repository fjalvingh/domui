package to.etc.domuidemo.pages.tutorial.binding;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.DateInput2;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Tutorial, "data binding", step 1: the same screen without binding - every
 * value is carried between the model and the controls by hand.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class BindByHandPage extends UrlPage {
	private final AlbumOrder m_order = new AlbumOrder();

	@Override
	public void createContent() throws Exception {
		setPageTitle("Carrying the values by hand");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Carrying the values by hand"));

		List<Genre> genreList = getSharedContext().query(QCriteria.create(Genre.class));

		Text2<String> customer = new Text2<>(String.class);
		ComboLookup2<Genre> genre = new ComboLookup2<>(genreList);
		DateInput2 delivery = new DateInput2();
		Text2<Integer> copies = new Text2<>(Integer.class);
		Text2<BigDecimal> price = new Text2<>(BigDecimal.class);

		//-- Every control has to be filled from the model, one call per control.
		customer.setValue(m_order.getCustomerName());
		genre.setValue(m_order.getGenre());
		delivery.setValue(m_order.getDeliveryDate());
		copies.setValue(m_order.getCopies());
		price.setValue(m_order.getPrice());

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Customer").control(customer);
		fb.label("Genre").control(genre);
		fb.label("Deliver on").control(delivery);
		fb.label("Copies").control(copies);
		fb.label("Price each").control(price);

		Div state = new Div("dm-tut");
		cp.add(new DefaultButton("Save", a -> {
			//-- ...and every one of them has to be carried back again.
			m_order.setCustomerName(customer.getValue());
			m_order.setGenre(genre.getValue());
			m_order.setDeliveryDate(delivery.getValue());
			m_order.setCopies(copies.getValue());
			m_order.setPrice(price.getValue());
			showOrder(state);
		}));
		cp.add(state);
		showOrder(state);
	}

	private void showOrder(Div state) {
		state.removeAllChildren();
		state.add(new HTag(2, "What the order holds"));
		line(state, "Customer: " + m_order.getCustomerName());
		line(state, "Genre: " + m_order.getGenre());
		line(state, "Deliver on: " + format(m_order.getDeliveryDate()));
		line(state, "Copies: " + m_order.getCopies());
		line(state, "Price each: " + m_order.getPrice());
	}

	private static void line(Div state, String text) {
		Div d = new Div();
		state.add(d);
		d.add(text);
	}

	private static String format(Date date) {
		return date == null ? "null" : new java.text.SimpleDateFormat("dd-MM-yyyy").format(date);
	}
}
