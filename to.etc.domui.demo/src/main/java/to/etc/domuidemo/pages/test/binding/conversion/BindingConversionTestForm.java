package to.etc.domuidemo.pages.test.binding.conversion;

import to.etc.domui.component.binding.IBindingConverter;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.misc.DisplayValue;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This form contains a binding between a Text&lt;String&gt; and an Integer
 * property. It uses an {@link IBindingConverter} to convert the types. When
 * conversion fails it should act as if validation failed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 19-3-17.
 */
public class BindingConversionTestForm extends UrlPage {
	@Nullable
	private Integer m_value;

	private Div m_div;

	final private class TestConverter implements IBindingConverter<String, Integer> {
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
				throw new ValidationException(Msgs.BUNDLE, Msgs.NOT_VALID, value);
			}
		}
	}

	@Override public void createContent() throws Exception {
		FormBuilder fb = new FormBuilder(this);
		Text<String> control = new Text<>(String.class);

		fb.property(this, "value").label("Integer").converter(new TestConverter()).control(control);

		DefaultButton db = new DefaultButton("click", new IClicked<DefaultButton>() {
			@Override public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				checkClickValue();
			}
		});
		add(db);

		db = new DefaultButton("setvalue", new IClicked<DefaultButton>() {
			@Override public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				setValue(Integer.valueOf(987));
			}
		});
		add(db);

		db = new DefaultButton("setnull", new IClicked<DefaultButton>() {
			@Override public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				setValue(null);
			}
		});
		add(db);
		add(m_div = new Div());
	}

	private void checkClickValue() throws Exception {
		if(bindErrors())
			return;
		Integer value = getValue();
		m_div.removeAllChildren();
		DisplayValue<Integer> nd = new DisplayValue<>(Integer.class, getValue());
		nd.setTestID("result");
		m_div.add(nd);
	}

	@Nullable
	public Integer getValue() {
		return m_value;
	}

	public void setValue(Integer value) {
		m_value = value;
	}
}
