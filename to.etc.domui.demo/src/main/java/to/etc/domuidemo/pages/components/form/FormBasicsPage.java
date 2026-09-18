package to.etc.domuidemo.pages.components.form;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.DateInput2;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;

import java.math.BigDecimal;

/**
 * FormBuilder: the label/control pair it is made of, and what a label can be
 * told before the control arrives.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class FormBasicsPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("FormBuilder: labels and controls");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "FormBuilder: labels and controls"));

		Text2<String> title = new Text2<>(String.class);
		Text2<Integer> copies = new Text2<>(Integer.class);
		Text2<BigDecimal> price = new Text2<>(BigDecimal.class);
		DateInput2 released = new DateInput2();
		Checkbox inPrint = new Checkbox();

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Album title").mandatory().control(title);
		fb.label("Copies in stock").control(copies);
		fb.label("Price each").control(price);
		fb.label("Released").control(released);
		fb.label("Still in print").control(inPrint);

		Div shown = new Div("dm-tut");
		shown.add("Press the button to read the five controls above.");

		cp.add(new DefaultButton("Read the form", ()-> {
			shown.removeAllChildren();
			shown.add("title=" + title.getValue()
				+ ", copies=" + copies.getValue()
				+ ", price=" + price.getValue()
				+ ", released=" + released.getValue()
				+ ", in print=" + inPrint.getValue());
		}));
		cp.add(shown);

		cp.add(new Para().add("The builder is not a component: it is handed the panel to "
			+ "build into, and every label() ... control() pair adds one row to it. The "
			+ "controls are made the ordinary way, before the form is built."));

		//-- The label itself
		cp.add(new HTag(2, "What the label can be"));

		Text2<String> plain = new Text2<>(String.class);
		Text2<String> marked = new Text2<>(String.class);
		Text2<String> made = new Text2<>(String.class);
		Text2<String> naked = new Text2<>(String.class);

		Span own = new Span();
		own.add("A label ");
		own.add(new Span("dm-tut-hi", "of your own"));

		FormBuilder fb2 = new FormBuilder(cp);
		fb2.label("An ordinary label").control(plain);
		fb2.label("Mandatory").mandatory().control(marked);
		fb2.label(own).control(made);
		fb2.controlOnly().control(naked);

		cp.add(new Para().add("mandatory() marks the label and tells the control a value "
			+ "is required. A label can also be a node you built yourself. The last row "
			+ "has no label at all: controlOnly() leaves it out, and the control then "
			+ "starts where the label would have been."));

		//-- Hints
		cp.add(new HTag(2, "Hints"));

		Text2<String> tooltip = new Text2<>(String.class);
		Text2<String> icon = new Text2<>(String.class);

		FormBuilder fb3 = new FormBuilder(cp);
		fb3.label("Catalogue number").hint("The number on the spine of the box").control(tooltip);

		FormBuilder fb4 = new FormBuilder(cp);
		fb4.hintAsIcon(true);
		fb4.label("Catalogue number").hint("The number on the spine of the box").control(icon);

		cp.add(new Para().add("Hover the first box to see its hint: without hintAsIcon() "
			+ "the hint is the control's own tooltip. With hintAsIcon(true) the hint "
			+ "becomes an icon behind the label, and clicking it shows the text."));

		//-- Things that are not controls
		cp.add(new HTag(2, "Something that is not a control"));

		Text2<String> comment = new Text2<>(String.class);
		Div note = new Div("dm-tut-q");
		note.add("Anything can sit in the control position - this is a Div.");

		FormBuilder fb5 = new FormBuilder(cp);
		fb5.label("A remark").control(comment);
		fb5.label("Not a control").item(note);

		cp.add(new Para().add("item() adds a node that is not an IControl. It is laid out "
			+ "like any other pair, but nothing is bound to it and nothing can be read "
			+ "from it."));
	}
}
