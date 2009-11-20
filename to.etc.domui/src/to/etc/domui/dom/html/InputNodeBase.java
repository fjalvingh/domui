package to.etc.domui.dom.html;

import to.etc.domui.dom.errors.*;

/**
 * This is a node which can handle input. It has helper stuff to convert input and to handle errors.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 12, 2008
 */
abstract public class InputNodeBase extends NodeBase implements IHasChangeListener, INodeErrorDelegate {
	private IValueChanged< ? > m_onValueChanged;

	@Override
	abstract public void visit(INodeVisitor v) throws Exception;

	public InputNodeBase(String tag) {
		super(tag);
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
