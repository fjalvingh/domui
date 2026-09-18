package to.etc.domuidemo.pages.components.display;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.DisplayControl;
import to.etc.domui.component.misc.DisplaySpan;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.MoneyBigDecimalFullConverter;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DisplaySpan and DisplayControl: showing a typed value that cannot be changed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DisplaySpanPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("DisplaySpan: showing a value");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "DisplaySpan: showing a value"));

		//-- The type decides how the value is rendered.
		DisplaySpan<String> title = new DisplaySpan<>(String.class);
		title.setValue("Rubber Soul");

		DisplaySpan<Integer> copies = new DisplaySpan<>(Integer.class);
		copies.setValue(12);

		DisplaySpan<Date> released = new DisplaySpan<>(Date.class);
		released.setValue(new Date());

		//-- A converter decides it instead.
		DisplaySpan<BigDecimal> price = new DisplaySpan<>(BigDecimal.class);
		price.setConverter(ConverterRegistry.getConverterInstance(MoneyBigDecimalFullConverter.class));
		price.setValue(new BigDecimal("14.95"));

		//-- Or a renderer, which may put anything at all inside the span.
		Album album = getSharedContext().query(QCriteria.create(Album.class).limit(1)).get(0);
		DisplaySpan<Album> rendered = new DisplaySpan<>(Album.class);
		rendered.setRenderer((node, value) -> {
			node.add(new Span("dm-tut-hi", value.getTitle()));
			node.add(" by " + value.getArtist().getName());
		});
		rendered.setValue(album);

		//-- An empty value shows the empty string, if one is set.
		DisplaySpan<String> empty = new DisplaySpan<>(String.class);
		empty.setEmptyString("(not known)");

		DisplaySpan<String> reallyEmpty = new DisplaySpan<>(String.class);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("A String").control(title);
		fb.label("An Integer").control(copies);
		fb.label("A Date").control(released);
		fb.label("With a money converter").control(price);
		fb.label("With a renderer").control(rendered);
		fb.label("Empty, with an empty string").control(empty);
		fb.label("Empty, without one").control(reallyEmpty);

		//-- The same thing as a div, which lines up in a form like a control does.
		cp.add(new HTag(2, "DisplayControl: the same as a div"));

		DisplayControl<String> asControl = new DisplayControl<>(String.class);
		asControl.setValue("Rubber Soul");

		DisplaySpan<String> asSpan = new DisplaySpan<>(String.class);
		asSpan.setValue("Rubber Soul");

		FormBuilder fb2 = new FormBuilder(cp);
		fb2.label("DisplayControl").control(asControl);
		fb2.label("DisplaySpan").control(asSpan);

		cp.add(new Para().add("Both show the same text. The DisplayControl is a div carrying "
			+ "the control css, so in a form it lines up with the input controls around it; "
			+ "the DisplaySpan is a span, which is what you want inside a sentence or a "
			+ "table cell."));
		cp.add(new Para().add("Neither can be typed in, and neither has a change event: "
			+ "getValue() hands back exactly what setValue() was given, whatever is on "
			+ "screen."));
	}
}
