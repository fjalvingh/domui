package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.ComboFixed;
import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.dom.html.NodeContainer;

import java.util.ArrayList;
import java.util.List;

public class ComboPropertyEditor implements IPropertyEditor {
	@NonNull
	final private PropertyDefinition m_pd;

	@NonNull
	final private ComboPropertyEditorFactory m_comboPropertyEditorFactory;

	@Nullable
	private Object m_value;

	public ComboPropertyEditor(@NonNull PropertyDefinition pd, @NonNull ComboPropertyEditorFactory comboPropertyEditorFactory) {
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
