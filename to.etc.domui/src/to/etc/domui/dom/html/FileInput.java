package to.etc.domui.dom.html;

import to.etc.domui.dom.errors.*;

public class FileInput extends NodeBase implements IHasChangeListener, INodeErrorDelegate {
	private IValueChanged< ? > m_onValueChanged;

	public FileInput() {
		super("input");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitFileInput(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}
}
