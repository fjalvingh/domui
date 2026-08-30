package to.etc.domuidemo.pages.tutorial.binding;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.DateInput2;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tutorial, "data binding", step 2: the same screen with bindings. Nothing is
 * carried by hand any more, and the read-only controls at the bottom are bound
 * to the very same properties, so they show what the model holds right now.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class BindValuePage extends UrlPage {
	private final AlbumOrder m_order = new AlbumOrder();

	@Override
	public void createContent() throws Exception {
		setPageTitle("The same screen, bound");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "The same screen, bound"));

		List<Genre> genreList = getSharedContext().query(QCriteria.create(Genre.class));

		Text2<String> customer = new Text2<>(String.class);
		ComboLookup2<Genre> genre = new ComboLookup2<>(genreList);
		DateInput2 delivery = new DateInput2();
		Text2<Integer> copies = new Text2<>(Integer.class);
		Text2<BigDecimal> price = new Text2<>(BigDecimal.class);

		customer.bind().to(m_order, AlbumOrder_.customerName());
		genre.bind().to(m_order, AlbumOrder_.genre());
		delivery.bind().to(m_order, AlbumOrder_.deliveryDate());
		copies.bind().to(m_order, AlbumOrder_.copies());
		price.bind().to(m_order, AlbumOrder_.price());

		//-- Send the value as soon as the user leaves the field, so the mirror below follows at once.
		customer.immediate();
		genre.immediate();
		delivery.immediate();
		copies.immediate();
		price.immediate();

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Customer").control(customer);
		fb.label("Genre").control(genre);
		fb.label("Deliver on").control(delivery);
		fb.label("Copies").control(copies);
		fb.label("Price each").control(price);

		cp.add(new DefaultButton("Clear the price", a -> m_order.setPrice(BigDecimal.ZERO)));

		//-- The same five properties again, as read-only controls that FormBuilder
		//-- makes and binds itself - one line each.
		cp.add(new HTag(2, "What the order holds"));

		FormBuilder fb2 = new FormBuilder(cp);
		fb2.property(m_order, AlbumOrder_.customerName()).readOnly().control();
		fb2.property(m_order, AlbumOrder_.genre()).readOnly().control();
		fb2.property(m_order, AlbumOrder_.deliveryDate()).readOnly().control();
		fb2.property(m_order, AlbumOrder_.copies()).readOnly().control();
		fb2.property(m_order, AlbumOrder_.price()).readOnly().control();
	}
}
