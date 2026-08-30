package to.etc.domuidemo.pages.tutorial.components;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.DateInput2;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

import java.util.Date;
import java.util.List;

/**
 * Tutorial, "using components", step 2: the state every control has -
 * value, readOnly, disabled and disabledBecause - and what each of them
 * does to the control on the screen.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ComponentStatePage extends UrlPage {
	private boolean m_readOnly;

	private boolean m_disabled;

	@Nullable
	private String m_disabledBecause;

	@Override
	public void createContent() throws Exception {
		setPageTitle("Control state");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "readOnly, disabled and disabledBecause"));

		//-- setValue() fills a control; each of them shows its value in its own way.
		Text2<String> title = new Text2<>(String.class);
		title.setValue("Kind of Blue");

		DateInput2 released = new DateInput2();
		released.setValue(new Date());

		ComboFixed2<String> medium = new ComboFixed2<>(List.of(
			new ValueLabelPair<>("cd", "Compact disc"),
			new ValueLabelPair<>("lp", "Vinyl LP"),
			new ValueLabelPair<>("dl", "Download")
		));
		medium.setValue("lp");

		String because = m_disabledBecause;
		if(because != null) {
			//-- A reason both disables the control and becomes its hover text.
			title.setDisabledBecause(because);
			released.setDisabledBecause(because);
			medium.setDisabledBecause(because);
		} else if(m_disabled) {
			title.setDisabled(true);
			released.setDisabled(true);
			medium.setDisabled(true);
		}
		if(m_readOnly) {
			title.setReadOnly(true);
			released.setReadOnly(true);
			medium.setReadOnly(true);
		}

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Album title").control(title);
		fb.label("Released").control(released);
		fb.label("Medium").control(medium);

		Div buttons = new Div();
		cp.add(buttons);
		buttons.add(new DefaultButton("Editable", a -> state(false, false, null)));
		buttons.add(new DefaultButton("Read only", a -> state(true, false, null)));
		buttons.add(new DefaultButton("Disabled", a -> state(false, true, null)));
		buttons.add(new DefaultButton("Disabled because", a -> state(false, false, "This album is no longer for sale")));

		Para hint = new Para();
		cp.add(hint);
		hint.add("After 'Disabled because', hover over a control to see the reason it is off.");
	}

	/**
	 * Remember the wanted state and build the page again with it.
	 */
	private void state(boolean readOnly, boolean disabled, @Nullable String because) {
		m_readOnly = readOnly;
		m_disabled = disabled;
		m_disabledBecause = because;
		forceRebuild();
	}
}
