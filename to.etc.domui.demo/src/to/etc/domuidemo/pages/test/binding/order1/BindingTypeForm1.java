package to.etc.domuidemo.pages.test.binding.order1;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.misc.DisplayValue;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 19-3-17.
 */
public class BindingTypeForm1 extends UrlPage {
	private Integer m_value;

	private Div m_div;

	@Override public void createContent() throws Exception {
		FormBuilder fb = new FormBuilder(this);

		Text<String> control = new Text<>(String.class);

		fb.property(this, "value").label("Integer").control(control);

		DefaultButton db = new DefaultButton("click", new IClicked<DefaultButton>() {
			@Override public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				checkClickValue();
			}
		});
		add(db);
		add(m_div = new Div());
	}

	private void checkClickValue() {
		Integer value = getValue();
		m_div.removeAllChildren();
		m_div.add(new DisplayValue<Integer>(getValue()));
	}

	public Integer getValue() {
		return m_value;
	}

	public void setValue(Integer value) {
		m_value = value;
	}
}
