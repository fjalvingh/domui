package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * A property binding specific for display-only controls. This binding will
 * abort any attempt to put the associated displayonly field into a state
 * that is not allowed (like setting it to editable, enabled or not-readonly).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 15, 2010
 */
public class DisplayOnlyPropertyBinding<T> implements IModelBinding, IFormControl {
	final IDisplayControl<Object> m_control;

	private PropertyMetaModel m_propertyMeta;

	private IReadOnlyModel< ? > m_model;

	public DisplayOnlyPropertyBinding(IReadOnlyModel< ? > model, PropertyMetaModel propertyMeta, IDisplayControl<T> control) {
		m_model = model;
		m_propertyMeta = propertyMeta;
		m_control = (IDisplayControl<Object>) control;
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

	public void setControlsEnabled(boolean on) {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IDisplayControl interface									*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 */
	public Object getValue() {
		return m_control.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(Object value) {
		m_control.setValue(value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	public Object getValueSafe() {
		return m_control.getValue();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOnValueChanged(IValueChanged< ? > listener) {
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#hasError()
	 */
	@Override
	public boolean hasError() {
		return false;
		//		return m_control.hasError();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IActionControl#setDisabled(boolean)
	 */
	@Override
	public void setDisabled(boolean d) {
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#setMandatory(boolean)
	 */
	@Override
	public void setMandatory(boolean ro) {
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#setReadOnly(boolean)
	 */
	@Override
	public void setReadOnly(boolean ro) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTestID(String testID) {
		m_control.setTestID(testID);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	INodeErrorDelegate interface.						*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.dom.errors.INodeErrorDelegate#clearMessage()
	 */
	public void clearMessage() {
	//		m_control.clearMessage();
	}

	public UIMessage getMessage() {
		return null;
		//		return m_control.getMessage();
	}

	public UIMessage setMessage(UIMessage m) {
		throw new IllegalStateException("Attempt to set an error message on a display-only control: " + m_control + ", bound on " + m_propertyMeta);
		//		return m_control.setMessage(m);
	}

	@Override
	public String getErrorLocation() {
		return null;
		//		return m_control.getErrorLocation();
	}

	@Override
	public void setErrorLocation(String errorLocation) {
		throw new IllegalStateException("Attempt to set an error location on a display-only control: " + m_control + ", bound on " + m_propertyMeta);
		//		m_control.setErrorLocation(errorLocation);
	}
}
