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
public class DisplayOnlyPropertyBinding<T> implements IModelBinding, IControl<T> {
	final IDisplayControl<T> m_control;

	private PropertyMetaModel m_propertyMeta;

	private IReadOnlyModel< ? > m_model;

	public DisplayOnlyPropertyBinding(IReadOnlyModel< ? > model, PropertyMetaModel propertyMeta, IDisplayControl<T> control) {
		m_model = model;
		m_propertyMeta = propertyMeta;
		m_control = control;
	}

	@Override
	public void moveControlToModel() throws Exception {
		Object val = m_control.getValue();
		Object base = m_model.getValue();
		IValueAccessor<Object> a = (IValueAccessor<Object>) m_propertyMeta.getAccessor();
		a.setValue(base, val);
	}

	@Override
	public void moveModelToControl() throws Exception {
		Object base = m_model.getValue();
		IValueAccessor< ? > vac = m_propertyMeta.getAccessor();
		if(vac == null)
			throw new IllegalStateException("Null IValueAccessor<T> returned by PropertyMeta " + m_propertyMeta);
		T pval = (T) m_propertyMeta.getAccessor().getValue(base);
		m_control.setValue(pval);
	}

	@Override
	public void setControlsEnabled(boolean on) {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IDisplayControl interface									*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getValue() {
		return m_control.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(T value) {
		m_control.setValue(value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	public T getValueSafe() {
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
	@Override
	public void setOnValueChanged(IValueChanged< ? > listener) {
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#hasError()
	 */
	@Override
	public boolean hasError() {
		//It is possible to set visual error marker onto data that is not editable directly
		if(m_control instanceof NodeBase) {
			return ((NodeBase) m_control).hasError();
		}
		return false;
	}

	@Override
	public boolean isDisabled() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IActionControl#setDisabled(boolean)
	 */
	@Override
	public void setDisabled(boolean d) {
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#setMandatory(boolean)
	 */
	@Override
	public void setMandatory(boolean ro) {
	}

	@Override
	public boolean isReadOnly() {
		return true;
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
	@Override
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
	@Override
	public void clearMessage() {
		//It is possible to set visual error marker onto data that is not editable directly
		if(m_control instanceof NodeBase) {
			((NodeBase) m_control).clearMessage();
		}
	}

	@Override
	public UIMessage getMessage() {
		//It is possible to set visual error marker onto data that is not editable directly
		if(m_control instanceof NodeBase) {
			return ((NodeBase) m_control).getMessage();
		}
		return null;
	}

	@Override
	public UIMessage setMessage(UIMessage m) {
		//It is possible to set visual error marker onto data that is not editable directly
		if(m_control instanceof NodeBase) {
			return ((NodeBase) m_control).setMessage(m);
		} else {
			throw new IllegalStateException("Attempt to set an error message on a display-only control: " + m_control + ", bound on " + m_propertyMeta);
		}
	}

	@Override
	public String getErrorLocation() {
		//It is possible to set visual error marker onto data that is not editable directly
		if(m_control instanceof NodeBase) {
			return ((NodeBase) m_control).getErrorLocation();
		}
		return null;
	}

	@Override
	public void setErrorLocation(String errorLocation) {
		//It is possible to set visual error marker onto data that is not editable directly
		if(m_control instanceof NodeBase) {
			((NodeBase) m_control).setErrorLocation(errorLocation);
		}
		throw new IllegalStateException("Attempt to set an error location on a display-only control: " + m_control + ", bound on " + m_propertyMeta);
	}
}
