package to.etc.domuidemo.pages.components.input;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.graph.ColorPickerInput;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * ColorPickerInput: the colour code typed or picked, with a swatch beside it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ColorPickerInputPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("ColorPickerInput: the code and a swatch");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "ColorPickerInput: the code and a swatch"));

		Div shown = new Div("dm-tut");
		shown.add("Click in the box to open the picker, or type a hex code.");

		ColorPickerInput cover = new ColorPickerInput();
		cover.setValue("c05a2a");
		cover.setOnValueChanged(a -> {
			shown.removeAllChildren();
			shown.add("The sleeve colour is now " + cover.getValue());
		});

		ColorPickerInput optional = new ColorPickerInput();
		optional.setMandatory(false);

		ColorPickerInput readOnly = new ColorPickerInput();
		readOnly.setValue("4a7ebb");
		readOnly.setReadOnly(true);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Sleeve colour").control(cover);
		fb.label("setMandatory(false)").control(optional);
		fb.label("setReadOnly(true)").control(readOnly);

		cp.add(new DefaultButton("What is in the optional one?", a -> {
			shown.removeAllChildren();
			shown.add("The optional box holds: " + optional.getValue());
		}));
		cp.add(shown);

		cp.add(new Para().add("The control is mandatory by default: leave it empty and it "
			+ "hands back black. Only setMandatory(false) lets it hand back null."));
		cp.add(new Para().add("The read-only one keeps its swatch but does not open the "
			+ "picker."));
	}
}
