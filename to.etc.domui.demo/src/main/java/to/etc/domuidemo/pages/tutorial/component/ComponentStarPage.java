package to.etc.domuidemo.pages.tutorial.component;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "writing a component", step 1: a control of your own, used like any
 * other control - set a value, ask for a value, hear about a change, switch it off.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ComponentStarPage extends UrlPage {
	private boolean m_readOnly;

	private boolean m_disabled;

	private boolean m_mandatory;

	@Override
	public void createContent() throws Exception {
		setPageTitle("A control of your own");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A control of your own"));

		StarRating rating = new StarRating();
		rating.setReadOnly(m_readOnly);
		rating.setDisabled(m_disabled);
		rating.setMandatory(m_mandatory);

		Div result = new Div("dm-tut");

		//-- It is an IControl, so it has a change event like every other control.
		rating.setOnValueChanged(a -> say(result, "changed to " + rating.getValue()));

		FormBuilder fb = new FormBuilder(cp);
		fb.label("How good is it?").control(rating);

		Div buttons = new Div();
		cp.add(buttons);
		buttons.add(new DefaultButton("Read the value", a -> say(result, "the value is " + rating.getValue())));
		buttons.add(new DefaultButton("Set it to 3", a -> rating.setValue(3)));
		buttons.add(new DefaultButton("Set it to 3 again", a -> {
			rating.setValue(3);                            // Does nothing when it already is 3
			say(result, "setValue(3) called; the control only redraws when the value really changed");
		}));
		buttons.add(new DefaultButton("Clear it", a -> rating.setValue(null)));

		buttons.add(new DefaultButton(m_readOnly ? "readOnly off" : "readOnly on", a -> {
			m_readOnly = !m_readOnly;
			forceRebuild();
		}));
		buttons.add(new DefaultButton(m_disabled ? "disabled off" : "disabled on", a -> {
			m_disabled = !m_disabled;
			forceRebuild();
		}));
		buttons.add(new DefaultButton(m_mandatory ? "mandatory off" : "mandatory on", a -> {
			m_mandatory = !m_mandatory;
			forceRebuild();
		}));

		cp.add(result);
		result.add("Click a star, or press one of the buttons.");
	}

	private static void say(Div result, String what) {
		result.removeAllChildren();
		result.add(what);
	}
}
