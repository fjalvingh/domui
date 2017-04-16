package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.component.graph.*;
import to.etc.domui.dom.html.*;

public class ColorPropertyEditor implements IPropertyEditor {
	@Nonnull
	final private PropertyDefinition m_pd;

	private Object m_value;

	private ColorPickerInput m_control;

	public ColorPropertyEditor(@Nonnull PropertyDefinition pd) {
		m_pd = pd;

	}

	@Override
	public void renderValue(@Nonnull NodeContainer target) throws Exception {
		ColorPickerInput txt = m_control = new ColorPickerInput();
		target.add(m_control);
		if(m_value != null)
			txt.setValue(String.valueOf(m_value));
	}

	@Override
	public void setValue(Object value) {
		m_value = value;
	}
}
