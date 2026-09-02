package to.etc.domuidemo.pages.components.choice;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.RadioGroup;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;

/**
 * RadioGroup: one value out of a handful, with every choice visible.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class RadioGroupPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("RadioGroup: one out of a few");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "RadioGroup: one out of a few"));

		Div shown = new Div("dm-tut");
		shown.add("Pick a medium and press the button.");

		//-- Buttons added one by one, with the label given here.
		RadioGroup<Medium> byHand = new RadioGroup<>();
		byHand.addButton("Compact disc", Medium.Cd);
		byHand.addButton("Vinyl LP", Medium.Vinyl);
		byHand.addButton("Download", Medium.Download);
		byHand.setValue(Medium.Cd);

		//-- Every value of the enum, labels from Medium.properties, sorted by label.
		RadioGroup<Medium> fromEnum = RadioGroup.createEnumRadioGroup(Medium.class);
		fromEnum.setMandatory(true);

		//-- The same, in declaration order, and drawn as a row of buttons.
		RadioGroup<Medium> asButtons = RadioGroup
			.createEnumRadioGroupUnsorted(Medium.class, Medium.Cassette)
			.asButtons();

		//-- A renderer of your own decides what stands next to each button.
		RadioGroup<Medium> rendered = RadioGroup.createEnumRadioGroupUnsorted(Medium.class);
		rendered.setValueRenderer((node, item) -> {
			node.add(new Span("dm-tut-hi", item.getLabelText()));
			node.add(" (" + item.getValue().name() + ")");
		});

		RadioGroup<Medium> readOnly = RadioGroup.createEnumRadioGroupUnsorted(Medium.class);
		readOnly.setValue(Medium.Vinyl);
		readOnly.setReadOnly(true);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("addButton() three times").control(byHand);
		fb.label("From the enum, mandatory").control(fromEnum);
		fb.label("asButtons(), Cassette left out").control(asButtons);
		fb.label("With a value renderer").control(rendered);
		fb.label("setReadOnly(true)").control(readOnly);

		cp.add(new DefaultButton("Read the values", a -> {
			//-- The mandatory group throws when nothing is picked.
			Medium mandatory = fromEnum.getValue();
			shown.removeAllChildren();
			shown.add("by hand=" + byHand.getValue()
				+ ", from enum=" + mandatory
				+ ", as buttons=" + asButtons.getValue()
				+ ", read only=" + readOnly.getValue());
		}));
		cp.add(shown);

		cp.add(new Para().add("The group is the control: the buttons inside it have values, "
			+ "but only the group has a value, a mandatory state and a binding."));
		cp.add(new Para().add("Leave the mandatory group untouched and press the button: it "
			+ "reports Mandatory field and the handler stops there."));
	}
}
