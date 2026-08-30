package to.etc.domuidemo.pages.tutorial.components;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.DateInput2;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Tutorial, "using components", step 1: a form of components, and what
 * getValue() does when the input cannot be delivered.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ComponentFormPage extends UrlPage {
	private final Text2<String> m_title = new Text2<>(String.class);

	private final Text2<Integer> m_copies = new Text2<>(Integer.class);

	private final Text2<BigDecimal> m_price = new Text2<>(BigDecimal.class);

	private final DateInput2 m_released = new DateInput2();

	private final ComboFixed2<String> m_medium = new ComboFixed2<>(List.of(
		new ValueLabelPair<>("cd", "Compact disc"),
		new ValueLabelPair<>("lp", "Vinyl LP"),
		new ValueLabelPair<>("dl", "Download")
	));

	@Override
	public void createContent() throws Exception {
		setPageTitle("A form of components");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A form of components"));

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Album title").mandatory().control(m_title);
		fb.label("Copies in stock").control(m_copies);
		fb.label("Price each").control(m_price);
		fb.label("Released").control(m_released);
		fb.label("Medium").control(m_medium);

		Div result = new Div("dm-tut");
		result.add("Press a button to see what the controls hold");

		cp.add(new DefaultButton("Show the values", a -> {
			//-- Every getValue() can fail: the first one that does ends this handler.
			String title = m_title.getValue();
			Integer copies = m_copies.getValue();
			BigDecimal price = m_price.getValue();
			Date released = m_released.getValue();
			String medium = m_medium.getValue();

			result.removeAllChildren();
			line(result, "Title: " + title);
			line(result, "Copies: " + copies);
			line(result, "Price: " + price);
			line(result, "Released: " + (released == null ? null : new SimpleDateFormat("dd-MM-yyyy").format(released)));
			line(result, "Medium: " + medium);
		}));

		cp.add(new DefaultButton("Which fields are wrong?", a -> {
			result.removeAllChildren();
			line(result, "Title is " + (m_title.hasError() ? "wrong or missing" : "ok"));
			line(result, "Copies is " + (m_copies.hasError() ? "wrong or missing" : "ok"));
			line(result, "Price is " + (m_price.hasError() ? "wrong or missing" : "ok"));
			line(result, "Released is " + (m_released.hasError() ? "wrong or missing" : "ok"));
			line(result, "Medium is " + (m_medium.hasError() ? "wrong or missing" : "ok"));
		}));

		cp.add(result);
	}

	private static void line(Div into, String text) {
		Para para = new Para();
		into.add(para);
		para.add(text);
	}
}
