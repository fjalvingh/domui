package to.etc.domuidemo.pages.components.choice;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * Checkbox: a yes/no control whose value is a Boolean, and which reports a
 * change through its click handler.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class CheckboxPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Checkbox: yes or no");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Checkbox: yes or no"));

		Div shown = new Div("dm-tut");
		shown.add("Tick the first box: its click handler runs at once.");

		Checkbox newsletter = new Checkbox();
		newsletter.setChecked(true);
		newsletter.setClicked(a -> {
			shown.removeAllChildren();
			shown.add("Newsletter is now " + newsletter.getValue());
		});

		Checkbox silent = new Checkbox();

		Checkbox disabled = new Checkbox();
		disabled.setChecked(true);
		disabled.setDisabled(true);

		Checkbox because = new Checkbox();
		because.setDisabledBecause("Only the shop owner may change this");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Send me the newsletter").control(newsletter);
		fb.label("No handler at all").control(silent);
		fb.label("setDisabled(true)").control(disabled);
		fb.label("setDisabledBecause()").control(because);

		cp.add(new DefaultButton("Read all four", a -> {
			shown.removeAllChildren();
			shown.add("newsletter=" + newsletter.getValue()
				+ ", silent=" + silent.getValue()
				+ ", disabled=" + disabled.getValue()
				+ ", because=" + because.getValue());
		}));
		cp.add(shown);

		cp.add(new Para().add("The value of a checkbox is never null: an unticked box is "
			+ "Boolean.FALSE. There is nothing for it to fail on, so getValue() cannot "
			+ "report an error the way a Text2 does."));
		cp.add(new Para().add("A checkbox has no read-only state either: setReadOnly() is "
			+ "setDisabled()."));
	}
}
