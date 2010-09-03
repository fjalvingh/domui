package to.etc.domui.dom.html;

abstract public class InputNodeContainer extends NodeContainer implements IHasChangeListener {
	private IValueChanged< ? > m_onValueChanged;

	private boolean m_readOnly;

	private boolean m_mandatory;

	@Override
	abstract public void visit(INodeVisitor v) throws Exception;

	public InputNodeContainer(String tag) {
		super(tag);
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	protected void callOnValueChanged() throws Exception {
		if(m_onValueChanged != null) {
			IValueChanged<InputNodeContainer> vc = (IValueChanged<InputNodeContainer>) m_onValueChanged;
			vc.onValueChanged(this);
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
