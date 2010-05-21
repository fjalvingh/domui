package to.etc.domui.component.input;

import to.etc.domui.dom.html.*;

/**
 * Abstract base class for a control that is implemented on top of a DIV. This handles most basic actions required of
 * all controls.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 19, 2010
 */
abstract public class AbstractDivControl<T> extends Div implements IControl<T> {
	private boolean m_readOnly;

	private boolean m_disabled;

	private boolean m_mandatory;

	private IValueChanged< ? > m_valueChanged;

	private T m_value;

	@Override
	abstract public void createContent() throws Exception;

	@Override
	public T getValueSafe() {
		try {
			return getValue();
		} catch(Exception x) {
			return null;
		}
	}

	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	@Override
	public void setMandatory(boolean m) {
		if(m_mandatory == m)
			return;
		m_mandatory = m;
		mandatoryChanged();
	}

	protected void mandatoryChanged() {
		forceRebuild();
	}

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override
	public void setReadOnly(boolean ro) {
		if(ro == m_readOnly)
			return;
		m_readOnly = ro;
		readOnlyChanged();
	}

	protected void readOnlyChanged() {
		forceRebuild();
	}

	@Override
	public T getValue() {
		validate();
		return m_value;
	}

	protected void validate() {
	}

	@Override
	public void setValue(T v) {
		m_value = v;
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean d) {
		if(m_disabled == d)
			return;
		m_disabled = d;
		disabledChanged();
	}

	protected void disabledChanged() {
		forceRebuild();
	}

	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_valueChanged;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_valueChanged = onValueChanged;
	}
}
