package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class SimpleComponentPropertyBinding implements ModelBinding, IFormControl {
	final IInputNode<Object> m_control;

	private PropertyMetaModel m_propertyMeta;

	private IReadOnlyModel< ? > m_model;

	public SimpleComponentPropertyBinding(IReadOnlyModel< ? > model, PropertyMetaModel propertyMeta, IInputNode< ? > control) {
		m_model = model;
		m_propertyMeta = propertyMeta;
		m_control = (IInputNode<Object>) control;
	}

	public void moveControlToModel() throws Exception {
		Object val = m_control.getValue();
		Object base = m_model.getValue();
		IValueAccessor<Object> a = (IValueAccessor<Object>) m_propertyMeta.getAccessor();
		a.setValue(base, val);
	}

	public void moveModelToControl() throws Exception {
		Object base = m_model.getValue();
		IValueAccessor< ? > vac = m_propertyMeta.getAccessor();
		if(vac == null)
			throw new IllegalStateException("Null IValueAccessor<T> returned by PropertyMeta " + m_propertyMeta);
		Object pval = m_propertyMeta.getAccessor().getValue(base);
		m_control.setValue(pval);
	}

	public void setEnabled(boolean on) {
		m_control.setReadOnly(!on);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IFormControl interface								*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.component.form.IFormControl#getValue()
	 */
	public Object getValue() {
		return m_control.getValue();
	}

	public void setOnValueChanged(IValueChanged<NodeBase, Object> listener) {
		m_control.setOnValueChanged(listener);
	}

	public void setValue(Object value) {
		m_control.setValue(value);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	INodeErrorDelegate interface.						*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.dom.errors.INodeErrorDelegate#clearMessage()
	 */
	public void clearMessage() {
		m_control.clearMessage();
	}

	public UIMessage getMessage() {
		return m_control.getMessage();
	}

	public UIMessage setMessage(UIMessage m) {
		return m_control.setMessage(m);
	}

	public void setTestID(String testID) {
		m_control.setTestID(testID);
	}
}
