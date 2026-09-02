package to.etc.domuidemo.pages.components.buttons;

import to.etc.domui.component.buttons.CheckboxButton;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.SwitchButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * CheckboxButton and SwitchButton: two buttons whose value is a Boolean.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ToggleButtonPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("CheckboxButton and SwitchButton");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "CheckboxButton and SwitchButton"));

		Div shown = new Div("dm-tut-q");
		shown.add("Flip one of them.");

		CheckboxButton plain = new CheckboxButton();
		plain.setChecked(true);
		plain.setClicked(a -> say(shown, "Newsletter is now " + plain.isChecked()));

		CheckboxButton labels = new CheckboxButton()
			.setOnLabel("In stock")
			.setOffLabel("Sold out");

		CheckboxButton small = new CheckboxButton().css("is-small");
		CheckboxButton large = new CheckboxButton().css("is-large");

		CheckboxButton disabled = new CheckboxButton();
		disabled.setChecked(true);
		disabled.setDisabled(true);

		SwitchButton rounded = new SwitchButton();
		rounded.setChecked(true);

		SwitchButton square = new SwitchButton();
		square.setDisplayMode(SwitchButton.DisplayMode.Square);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Send me the newsletter").control(plain);
		fb.label("Own on/off labels").control(labels);
		fb.label("is-small").control(small);
		fb.label("is-large").control(large);
		fb.label("setDisabled(true)").control(disabled);
		fb.label("SwitchButton, rounded").control(rounded);
		fb.label("SwitchButton, square").control(square);

		cp.add(new DefaultButton("Read the values", a -> {
			shown.removeAllChildren();
			shown.add("newsletter=" + plain.getValue()
				+ ", labelled=" + labels.getValue()
				+ ", switch=" + rounded.getValue());
		}));
		cp.add(shown);

		cp.add(new Para().add("Both are IControl<Boolean>, so they go in a form and can be "
			+ "bound like any other control. The difference is only what they look like: the "
			+ "CheckboxButton says what each side means, the SwitchButton is a switch and "
			+ "says nothing."));
		cp.add(new Para().add("Without on and off labels the CheckboxButton uses the "
			+ "framework's own Yes/No texts."));
	}

	private static void say(Div into, String what) {
		into.removeAllChildren();
		into.add(what);
	}
}
