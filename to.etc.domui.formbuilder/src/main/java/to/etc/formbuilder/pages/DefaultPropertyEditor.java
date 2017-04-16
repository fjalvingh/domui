package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;

public class DefaultPropertyEditor implements IPropertyEditor {
	@Nonnull
	final private PropertyDefinition m_pd;

	private Object m_value;

	private Text<String> m_control;

	public DefaultPropertyEditor(@Nonnull PropertyDefinition pd) {
		m_pd = pd;

	}

	@Override
	public void renderValue(@Nonnull NodeContainer target) throws Exception {
		Text<String> txt = m_control = new Text<String>(String.class);
		target.add(m_control);
		if(m_value != null)
			txt.setValue(String.valueOf(m_value));
	}

	@Override
	public void setValue(Object value) {
		m_value = value;
	}
}
