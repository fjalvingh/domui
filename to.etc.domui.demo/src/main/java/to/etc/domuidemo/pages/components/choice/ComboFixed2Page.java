package to.etc.domuidemo.pages.components.choice;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

import java.util.List;

/**
 * ComboFixed2: a drop-down over a list of values you state yourself.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ComboFixed2Page extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("ComboFixed2: a fixed list of choices");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "ComboFixed2: a fixed list of choices"));

		Div shown = new Div("dm-tut");
		shown.add("Change the first combo: its change handler runs when you leave it.");

		//-- The value and its label, stated per item.
		ComboFixed2<String> pairs = new ComboFixed2<>(List.of(
			new ValueLabelPair<>("cd", "Compact disc"),
			new ValueLabelPair<>("lp", "Vinyl LP"),
			new ValueLabelPair<>("dl", "Download")
		));
		pairs.setOnValueChanged(a -> {
			shown.removeAllChildren();
			shown.add("The code of the chosen medium is " + pairs.getValue());
		});

		//-- Every value of the enum, labels from Medium.properties.
		ComboFixed2<Medium> fromEnum = ComboFixed2.createEnumCombo(Medium.class);

		//-- Mandatory: no empty choice once a real value is selected.
		ComboFixed2<Medium> mandatory = ComboFixed2.createEnumCombo(Medium.class);
		mandatory.setMandatory(true);

		//-- Mandatory and built with a value: no empty choice at all.
		ComboFixed2<Medium> mandatoryFilled = ComboFixed2.createEnumCombo(Medium.class);
		mandatoryFilled.setMandatory(true);
		mandatoryFilled.setValue(Medium.Cd);

		//-- What the empty choice says.
		ComboFixed2<Medium> emptyText = ComboFixed2.createEnumCombo(Medium.class);
		emptyText.setEmptyText("- pick a medium -");

		//-- Read only: the combo renders as the label of its value, nothing else.
		ComboFixed2<Medium> readOnly = ComboFixed2.createEnumCombo(Medium.class);
		readOnly.setValue(Medium.Vinyl);
		readOnly.setReadOnly(true);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Value/label pairs").control(pairs);
		fb.label("From the enum").control(fromEnum);
		fb.label("Mandatory, still empty").control(mandatory);
		fb.label("Mandatory, built with a value").control(mandatoryFilled);
		fb.label("setEmptyText()").control(emptyText);
		fb.label("setReadOnly(true)").control(readOnly);

		cp.add(new DefaultButton("Read the values", a -> {
			Medium value = mandatory.getValue();
			shown.removeAllChildren();
			shown.add("pairs=" + pairs.getValue()
				+ ", enum=" + fromEnum.getValue()
				+ ", mandatory=" + value
				+ ", empty text=" + emptyText.getValue()
				+ ", read only=" + readOnly.getValue());
		}));
		cp.add(shown);

		cp.add(new Para().add("Every combo that is not mandatory carries an extra empty "
			+ "choice at the top, which is how it can hand back null. A mandatory combo "
			+ "renders that choice only while it has no valid value at the moment it is "
			+ "built: the fourth combo has one and does not offer it. Picking a value in "
			+ "the browser does not take the empty choice away - that needs a rebuild."));
		cp.add(new Para().add("The labels of the enum combos come from Medium.properties, "
			+ "not from the code: the values are Cd, Vinyl, Download and Cassette."));
	}
}
