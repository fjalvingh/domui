package to.etc.domuidemo.pages.test.binding.conversion;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.binding.IBidiBindingConverter;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.DisplaySpan;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;

/**
 * This form contains a binding between a Text2&lt;String&gt; and an Integer
 * property. It uses an {@link IBidiBindingConverter} to convert the types. When
 * conversion fails it should act as if validation failed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 19-3-17.
 */
public class BindingConversionTestForm extends UrlPage {
	@Nullable
	private Integer m_value;

	final private class TestConverter implements IBidiBindingConverter<String, Integer> {
		@Nullable @Override public String modelToControl(@Nullable Integer value) throws Exception {
			if(null == value)
				return null;
			return value.toString();
		}

		@Nullable @Override public Integer controlToModel(@Nullable String value) throws Exception {
			if(null == value)
				return null;
			try {
				return Integer.decode(value.trim());
			} catch(Exception x) {
				throw new ValidationException(Msgs.notValid, value);
			}
		}
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		FormBuilder fb = new FormBuilder(cp);
		Text2<String> control = new Text2<>(String.class);

		fb.property(this, "value").label("Integer").control(control, new TestConverter());

		Div result = new Div();

		cp.add(new DefaultButton("click", ()-> checkClickValue(result)));
		cp.add(new DefaultButton("setvalue", ()-> setValue(Integer.valueOf(987))));
		cp.add(new DefaultButton("setnull", ()-> setValue(null)));
		cp.add(result);
	}

	private void checkClickValue(Div result) throws Exception {
		if(bindErrors())
			return;
		result.removeAllChildren();
		DisplaySpan<Integer> nd = new DisplaySpan<>(Integer.class, getValue());
		nd.setTestID("result");
		result.add(nd);
	}

	@Nullable
	public Integer getValue() {
		return m_value;
	}

	public void setValue(Integer value) {
		m_value = value;
	}
}
