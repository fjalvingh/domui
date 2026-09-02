package to.etc.domuidemo.pages.components.input;

import to.etc.domui.component.graph.ColorPickerButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * ColorPickerButton: a swatch that opens the picker when pressed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ColorPickerButtonPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("ColorPickerButton: a swatch to press");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "ColorPickerButton: a swatch to press"));

		Div shown = new Div("dm-tut");
		shown.add("Press the button and pick a colour: the change arrives here by itself.");

		ColorPickerButton cover = new ColorPickerButton();
		cover.setValue("c05a2a");
		cover.setOnValueChanged(a -> {
			shown.removeAllChildren();
			shown.add("The sleeve colour is now " + cover.getValue());
		});

		ColorPickerButton disabled = new ColorPickerButton();
		disabled.setValue("4a7ebb");
		disabled.setDisabled(true);

		ColorPickerButton readOnly = new ColorPickerButton();
		readOnly.setValue("3a8a3a");
		readOnly.setReadOnly(true);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Sleeve colour").control(cover);
		fb.label("setDisabled(true)").control(disabled);
		fb.label("setReadOnly(true)").control(readOnly);
		cp.add(shown);

		cp.add(new Para().add("A change handler is what makes the button report back: without "
			+ "one the new colour simply waits on the server for the next request."));
		cp.add(new Para().add("The last two still show their colour, but pressing them opens "
			+ "nothing."));
	}
}
