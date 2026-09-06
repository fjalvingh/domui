package to.etc.domuidemo.pages.test.binding.order1;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.DisplaySpan;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;

/**
 * A Text2&lt;String&gt; bound to an Integer property, with no converter: building
 * this page must fail with a BindingDefinitionException.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 19-3-17.
 */
public class BindingTypeForm1 extends UrlPage {
	private Integer m_value;

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		FormBuilder fb = new FormBuilder(cp);

		Text2<String> control = new Text2<>(String.class);

		fb.property(this, "value").label("Integer").control(control);

		Div result = new Div();
		cp.add(new DefaultButton("click", a -> showValue(result)));
		cp.add(result);
	}

	private void showValue(Div result) {
		result.removeAllChildren();
		result.add(new DisplaySpan<>(Integer.class, getValue()));
	}

	public Integer getValue() {
		return m_value;
	}

	public void setValue(Integer value) {
		m_value = value;
	}
}
