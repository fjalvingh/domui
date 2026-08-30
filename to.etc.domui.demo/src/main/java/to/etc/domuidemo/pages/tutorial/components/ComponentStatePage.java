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
	private final Text2<String> m_title = new Text2<>(String.class);

	private final DateInput2 m_released = new DateInput2();

	private final ComboFixed2<String> m_medium = new ComboFixed2<>(List.of(
		new ValueLabelPair<>("cd", "Compact disc"),
		new ValueLabelPair<>("lp", "Vinyl LP"),
		new ValueLabelPair<>("dl", "Download")
	));

	@Override
	public void createContent() throws Exception {
		setPageTitle("Control state");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "readOnly, disabled and disabledBecause"));

		//-- setValue() fills a control; each of them shows its value in its own way.
		m_title.setValue("Kind of Blue");
		m_released.setValue(new Date());
		m_medium.setValue("lp");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Album title").control(m_title);
		fb.label("Released").control(m_released);
		fb.label("Medium").control(m_medium);

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

	private void state(boolean readOnly, boolean disabled, @Nullable String because) {
		m_title.setReadOnly(readOnly);
		m_released.setReadOnly(readOnly);
		m_medium.setReadOnly(readOnly);

		//-- A reason both disables the control and becomes its hover text; null enables it again.
		m_title.setDisabledBecause(because);
		m_released.setDisabledBecause(because);
		m_medium.setDisabledBecause(because);

		if(because == null) {
			m_title.setDisabled(disabled);
			m_released.setDisabled(disabled);
			m_medium.setDisabled(disabled);
		}
	}
}
