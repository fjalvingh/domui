package to.etc.domuidemo.pages.components.input;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * Text2: everything that decides how the box looks and what sits next to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class Text2LookPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Text2: size, marker and buttons");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Text2: size, marker and buttons"));

		//-- The box itself
		Text2<String> wide = new Text2<>(String.class);

		Text2<String> narrow = new Text2<>(String.class);
		narrow.setSize(10);
		narrow.setMaxLength(6);

		Text2<String> placeholder = new Text2<>(String.class);
		placeholder.setPlaceHolder("firstname.lastname@example.com");

		Text2<String> marker = new Text2<>(String.class);
		marker.setMarkerText("Search");

		Text2<String> secret = new Text2<String>(String.class).password();
		secret.setHint("The hint is the browser's own tooltip");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Default").control(wide);
		fb.label("setSize(10), setMaxLength(6)").control(narrow);
		fb.label("setPlaceHolder()").control(placeholder);
		fb.label("setMarkerText()").control(marker);
		fb.label("password(), setHint()").control(secret);

		cp.add(new Para().add("The placeholder is the browser's own; the marker is a "
			+ "background image DomUI renders, and both disappear as soon as the box "
			+ "holds something."));

		//-- Buttons attached to the control
		cp.add(new HTag(2, "Buttons on the right of the box"));

		Div shown = new Div("dm-tut");
		shown.add("Press one of the buttons attached to the box below.");

		Text2<String> withButtons = new Text2<>(String.class);
		withButtons.setValue("Yesterday");
		withButtons.addButtonSmall(Icon.faSearch, a -> {
			shown.removeAllChildren();
			shown.add("Search pressed, the box holds: " + withButtons.getValue());
		});
		withButtons.addButtonSmall(Icon.faEraser, a -> {
			withButtons.setValue(null);
			shown.removeAllChildren();
			shown.add("Erase pressed, the box is empty now");
		});

		Text2<String> withBigButton = new Text2<>(String.class);
		withBigButton.addButton(Icon.faEnvelope, a -> MsgBox2.on(this)
			.info("Sending to " + withBigButton.getValue()));

		FormBuilder fb2 = new FormBuilder(cp);
		fb2.label("addButtonSmall() twice").control(withButtons);
		fb2.label("addButton()").control(withBigButton);
		cp.add(shown);

		cp.add(new Para().add("A button added to a Text2 sits inside the control, so it "
			+ "lines up with the box and stays with it wherever the control is put."));
	}
}
