package to.etc.domuidemo.pages.components.form;

import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;

/**
 * FormBuilder: how the label/control pairs are laid out - the direction, where
 * a form ends, and what else can sit in a pair.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class FormLayoutPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("FormBuilder: laying the form out");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Laying the form out"));

		//-- Vertical, the default
		cp.add(new HTag(2, "Vertical, and horizontal"));

		Text2<String> street = new Text2<>(String.class);
		Text2<String> town = new Text2<>(String.class);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Street").control(street);
		fb.label("Town").control(town);

		Text2<String> day = new Text2<>(String.class);
		day.setSize(4);
		Text2<String> month = new Text2<>(String.class);
		month.setSize(4);
		Text2<String> year = new Text2<>(String.class);
		year.setSize(6);

		FormBuilder fb2 = new FormBuilder(cp);
		fb2.horizontal();
		fb2.label("Day").control(day);
		fb2.label("Month").control(month);
		fb2.label("Year").control(year);

		cp.add(new Para().add("A form is vertical unless it is told otherwise: the label "
			+ "sits in front of the control and every pair is a line of its own. In a "
			+ "horizontal form the label sits above the control and the pairs run "
			+ "across the page. horizontal() and vertical() can be called in the middle "
			+ "of a form as well; the direction changes from the next pair on."));

		//-- Where one form ends and the next begins
		cp.add(new HTag(2, "Ending a form"));

		Text2<String> first = new Text2<>(String.class);
		Text2<String> second = new Text2<>(String.class);
		Text2<String> third = new Text2<>(String.class);

		FormBuilder fb3 = new FormBuilder(cp);
		fb3.label("Before the break").control(first);
		fb3.nl();
		cp.add(new Para().add("This paragraph is not part of either form."));
		fb3.label("After the break").control(second);
		fb3.label("And another").control(third);

		cp.add(new Para().add("One FormBuilder normally builds one form. nl() ends the "
			+ "form it is building, so what is added to the panel next lands between the "
			+ "two, and the pair after it starts a new form. That is also what happens "
			+ "by itself when the direction changes."));

		//-- More than one control in a pair
		cp.add(new HTag(2, "More than one thing in a pair"));

		Text2<String> amount = new Text2<>(String.class);
		amount.setSize(8);
		Text2<String> currency = new Text2<>(String.class);
		currency.setSize(4);

		Text2<String> code = new Text2<>(String.class);
		SmallImgButton help = new SmallImgButton(Icon.faQuestionCircle,
			()-> MsgBox2.on(this).info("The code is printed on the back of the sleeve"));

		FormBuilder fb4 = new FormBuilder(cp);
		fb4.label("Price").control(amount);
		fb4.append().label("in").control(currency);
		fb4.label("Catalogue code").control(code);
		fb4.appendAfterControl(help);

		cp.add(new Para().add("append() puts the next pair inside the previous control's "
			+ "place instead of on a line of its own, label and all - which is how two "
			+ "controls end up belonging to one label. appendAfterControl() adds a node "
			+ "there without a pair around it, for the button that belongs to the "
			+ "control before it."));

		//-- Label width
		cp.add(new HTag(2, "How wide the labels are"));

		Text2<String> narrowOne = new Text2<>(String.class);
		Text2<String> wideOne = new Text2<>(String.class);

		FormBuilder fb5 = new FormBuilder(cp);
		fb5.label("An ordinary form").control(narrowOne);

		Div wide = new Div("ui-label-wide");
		cp.add(wide);
		FormBuilder fb6 = new FormBuilder(wide);
		fb6.label("Inside a ui-label-wide div").control(wideOne);

		cp.add(new Para().add("The labels of a vertical form share a minimum width, so "
			+ "that the controls line up under each other. Putting the form inside a div "
			+ "with the class ui-label-wide or ui-label-extrawide makes that width "
			+ "bigger, for a form whose labels are long."));

		//-- css of a single pair
		cp.add(new HTag(2, "The css of one pair"));

		Text2<String> marked = new Text2<>(String.class);

		FormBuilder fb7 = new FormBuilder(cp);
		fb7.label("A highlighted row").cssLabel("dm-tut-hi").cssControl("dm-tut-hi").control(marked);
		fb7.label("An ordinary row").item(new Span("Nothing added to this one"));

		cp.add(new Para().add("cssLabel() and cssControl() add a class to the label and "
			+ "the control part of one pair, for the row that has to look different from "
			+ "the rest of the form."));
	}
}
