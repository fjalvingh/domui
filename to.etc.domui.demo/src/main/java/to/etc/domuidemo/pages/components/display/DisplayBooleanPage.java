package to.etc.domuidemo.pages.components.display;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.DisplayCheckbox;
import to.etc.domui.component.misc.DisplayRadiobutton;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * DisplayCheckbox and DisplayRadiobutton: a yes or no that cannot be changed,
 * without the greyed-out look of a disabled control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DisplayBooleanPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Showing a yes or no");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Showing a yes or no"));

		DisplayCheckbox on = new DisplayCheckbox(Boolean.TRUE);
		DisplayCheckbox off = new DisplayCheckbox(Boolean.FALSE);
		DisplayCheckbox unset = new DisplayCheckbox();

		DisplayRadiobutton rbOn = new DisplayRadiobutton();
		rbOn.setValue(Boolean.TRUE);
		DisplayRadiobutton rbOff = new DisplayRadiobutton();
		rbOff.setValue(Boolean.FALSE);

		//-- Icons of your own, with their own css class.
		DisplayRadiobutton icons = new DisplayRadiobutton(Icon.faThumbsUp, "icon", Icon.faThumbsDown, "icon");
		icons.setValue(Boolean.TRUE);

		//-- What the alternative looks like: a real checkbox, disabled.
		Checkbox disabled = new Checkbox();
		disabled.setChecked(true);
		disabled.setDisabled(true);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("DisplayCheckbox, true").control(on);
		fb.label("DisplayCheckbox, false").control(off);
		fb.label("DisplayCheckbox, null").control(unset);
		fb.label("DisplayRadiobutton, true").control(rbOn);
		fb.label("DisplayRadiobutton, false").control(rbOff);
		fb.label("DisplayRadiobutton, own icons").control(icons);
		fb.label("A disabled Checkbox, for comparison").control(disabled);

		cp.add(new Para().add("A display checkbox is an image, and a display radio button is "
			+ "an icon: neither is an input, so neither can be focused, tabbed to or greyed "
			+ "out. That is the difference with the disabled checkbox at the bottom, which "
			+ "still looks like something the user might be allowed to press."));
		cp.add(new Para().add("A DisplayCheckbox with no value at all shows the same picture "
			+ "as false - it has two images, not three."));
	}
}
