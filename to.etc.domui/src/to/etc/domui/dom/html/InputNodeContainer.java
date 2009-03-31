package to.etc.domui.dom.html;

abstract public class InputNodeContainer extends NodeContainer implements IInputBase {
	private IValueChanged<?, ?>	m_onValueChanged;

	private boolean				m_readOnly;

	private boolean				m_mandatory;

	@Override
	abstract public void visit(NodeVisitor v) throws Exception;

	public InputNodeContainer(String tag) {
		super(tag);
	}

	/**
	 * @see to.etc.domui.dom.html.IInputBase#getOnValueChanged()
	 */
	public IValueChanged< ? , ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	/**
	 * @see to.etc.domui.dom.html.IInputBase#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	public void setOnValueChanged(IValueChanged< ? , ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	protected void	callOnValueChanged(Object value) throws Exception {
		if(m_onValueChanged != null) {
			IValueChanged<InputNodeContainer, Object>	vc = (IValueChanged<InputNodeContainer, Object>) m_onValueChanged;
			vc.onValueChanged(this, value);
		}
	}

	public boolean isReadOnly() {
		return m_readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		m_readOnly = readOnly;
	}
	public boolean isMandatory() {
		return m_mandatory;
	}
	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}
}
