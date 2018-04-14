package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.graph.ColorPickerInput;
import to.etc.domui.dom.html.NodeContainer;

public class ColorPropertyEditor implements IPropertyEditor {
	@NonNull
	final private PropertyDefinition m_pd;

	private Object m_value;

	private ColorPickerInput m_control;

	public ColorPropertyEditor(@NonNull PropertyDefinition pd) {
		m_pd = pd;

	}

	@Override
	public void renderValue(@NonNull NodeContainer target) throws Exception {
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
