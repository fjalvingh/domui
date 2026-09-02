package to.etc.domuidemo.pages.components.input;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

import java.math.BigDecimal;

/**
 * Text2: the type in the constructor decides what the control accepts and
 * what getValue() hands back.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class Text2Page extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Text2: a typed input box");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Text2: a typed input box"));

		Text2<String> title = new Text2<>(String.class);
		Text2<Integer> copies = new Text2<>(Integer.class);
		Text2<Long> catalog = new Text2<>(Long.class);
		Text2<Double> weight = new Text2<>(Double.class);
		Text2<BigDecimal> price = new Text2<>(BigDecimal.class);

		title.setValue("Rubber Soul");
		copies.setValue(12);
		catalog.setValue(1826742L);
		weight.setValue(0.18d);
		price.setValue(new BigDecimal("14.95"));

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Album title").control(title);
		fb.label("Copies in stock").control(copies);
		fb.label("Catalog number").control(catalog);
		fb.label("Weight in kg").control(weight);
		fb.label("Price").control(price);

		Div result = new Div("dm-tut");
		result.add("Press the button to see what each control hands back.");

		cp.add(new DefaultButton("Show the values", a -> {
			result.removeAllChildren();
			line(result, title.getValue());
			line(result, copies.getValue());
			line(result, catalog.getValue());
			line(result, weight.getValue());
			line(result, price.getValue());
		}));
		cp.add(result);

		cp.add(new Para().add("The three numeric fields refuse letters while you type: "
			+ "the constructor's type made them numeric. Try typing 'abc' in "
			+ "'Copies in stock', and try '1.234,56' in Price."));
	}

	private static void line(Div into, Object value) {
		Para para = new Para();
		into.add(para);
		para.add(value == null
			? "null"
			: value.getClass().getSimpleName() + " " + value);
	}
}
