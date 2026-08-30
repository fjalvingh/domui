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
	@Override
	public void createContent() throws Exception {
		setPageTitle("A form of components");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A form of components"));

		Text2<String> title = new Text2<>(String.class);
		Text2<Integer> copies = new Text2<>(Integer.class);
		Text2<BigDecimal> price = new Text2<>(BigDecimal.class);
		DateInput2 released = new DateInput2();
		ComboFixed2<String> medium = new ComboFixed2<>(List.of(
			new ValueLabelPair<>("cd", "Compact disc"),
			new ValueLabelPair<>("lp", "Vinyl LP"),
			new ValueLabelPair<>("dl", "Download")
		));

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Album title").mandatory().control(title);
		fb.label("Copies in stock").control(copies);
		fb.label("Price each").control(price);
		fb.label("Released").control(released);
		fb.label("Medium").control(medium);

		Div result = new Div("dm-tut");
		result.add("Press a button to see what the controls hold");

		cp.add(new DefaultButton("Show the values", a -> {
			//-- Every getValue() can fail: the first one that does ends this handler.
			String titleValue = title.getValue();
			Integer copiesValue = copies.getValue();
			BigDecimal priceValue = price.getValue();
			Date releasedValue = released.getValue();
			String mediumValue = medium.getValue();

			result.removeAllChildren();
			line(result, "Title: " + titleValue);
			line(result, "Copies: " + copiesValue);
			line(result, "Price: " + priceValue);
			line(result, "Released: " + (releasedValue == null ? null : new SimpleDateFormat("dd-MM-yyyy").format(releasedValue)));
			line(result, "Medium: " + mediumValue);
		}));

		cp.add(new DefaultButton("Which fields are wrong?", a -> {
			result.removeAllChildren();
			line(result, "Title is " + (title.hasError() ? "wrong or missing" : "ok"));
			line(result, "Copies is " + (copies.hasError() ? "wrong or missing" : "ok"));
			line(result, "Price is " + (price.hasError() ? "wrong or missing" : "ok"));
			line(result, "Released is " + (released.hasError() ? "wrong or missing" : "ok"));
			line(result, "Medium is " + (medium.hasError() ? "wrong or missing" : "ok"));
		}));

		cp.add(result);
	}

	private static void line(Div into, String text) {
		Para para = new Para();
		into.add(para);
		para.add(text);
	}
}
