package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class SimpleComponentPropertyBinding implements ModelBinding {
	final IInputNode<Object>	m_control;

	private PropertyMetaModel	m_propertyMeta;

	private IReadOnlyModel<?>	m_model;

	public SimpleComponentPropertyBinding(IReadOnlyModel< ? > model, PropertyMetaModel propertyMeta, IInputNode< ? > control) {
		m_model = model;
		m_propertyMeta = propertyMeta;
		m_control = (IInputNode<Object>)control;
	}

	public void moveControlToModel() throws Exception {
		Object val = m_control.getValue();
		Object	base = m_model.getValue();
		IValueAccessor<Object> a = (IValueAccessor<Object>)m_propertyMeta.getAccessor();
		a.setValue(base, val);
	}

	public void moveModelToControl() throws Exception {
		Object	base = m_model.getValue();
		Object	pval = m_propertyMeta.getAccessor().getValue(base);
		m_control.setValue(pval);
	}

	public void setEnabled(boolean on) {
		m_control.setReadOnly(! on);
	}
}
