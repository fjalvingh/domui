package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;

public class ComboPropertyEditor implements IPropertyEditor {
	@Nonnull
	final private PropertyDefinition m_pd;

	@Nonnull
	final private ComboPropertyEditorFactory m_comboPropertyEditorFactory;

	@Nullable
	private Object m_value;

	public ComboPropertyEditor(@Nonnull PropertyDefinition pd, @Nonnull ComboPropertyEditorFactory comboPropertyEditorFactory) {
		m_pd = pd;
		m_comboPropertyEditorFactory = comboPropertyEditorFactory;
	}

	@Override
	public void setValue(Object value) {
		m_value = value;
	}

	@Override
	public void renderValue(NodeContainer target) throws Exception {
		List<ValueLabelPair<Object>> list = new ArrayList<ValueLabelPair<Object>>();
		for(Object o : m_comboPropertyEditorFactory.getValues()) {
			list.add(new ValueLabelPair<Object>(o, String.valueOf(o)));
		}
		ComboFixed<Object> cf = new ComboFixed<Object>(list);
		target.add(cf);
		cf.setMandatory(m_comboPropertyEditorFactory.isRequired());
		cf.setValue(m_value);
	}


}
