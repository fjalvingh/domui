package to.etc.domui.dom.html;

import to.etc.domui.util.*;

/**
 * INCOMPLETE A full-coded select box: this is unsuitable for large amount of options.
 *
 * Handling the selected item is incomplete.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 11, 2008
 */
public class Select extends InputNodeContainer implements IHasModifiedIndication {
	private boolean m_multiple;

	private boolean m_disabled;

	private int m_size;

	private int m_selectedIndex;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	public Select() {
		super("select");
	}

	public Select(String... txt) {
		this();
		for(String s : txt) {
			add(new SelectOption(s));
		}
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitSelect(this);
	}

	@Override
	protected boolean canContain(NodeBase node) {
		return node instanceof SelectOption;
	}

	public boolean isMultiple() {
		return m_multiple;
	}

	public void setMultiple(boolean multiple) {
		if(m_multiple == multiple)
			return;
		m_multiple = multiple;
		changed();
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		changed();
	}

	public int getSize() {
		return m_size;
	}

	public void setSize(int size) {
		if(m_size == size)
			return;
		m_size = size;
		changed();
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		if(isReadOnly() == readOnly)
			return;
		changed();
		super.setReadOnly(readOnly);
	}

	public SelectOption getOption(int ix) {
		if(ix < 0 || ix >= getChildCount())
			throw new ArrayIndexOutOfBoundsException("The option index " + ix + " is invalid, the #options is " + getChildCount());
		return (SelectOption) getChild(ix);
	}

	@Override
	public boolean acceptRequestParameter(String[] values) throws Exception {
		String in = values[0];
		SelectOption selo = (SelectOption) getPage().findNodeByID(in);
		int nindex = selo == null ? -1 : findChildIndex(selo);
		int oldindex = m_selectedIndex;
		setSelectedIndex(nindex);
		if(oldindex == nindex)
			return false;
		DomUtil.setModifiedFlag(this);
		return true;
	}

	public void clearSelected() {
		m_selectedIndex = -1;
		for(int i = getChildCount(); --i >= 0;) {
			getOption(i).setSelected(false);
		}
	}

	public int getSelectedIndex() {
		return m_selectedIndex;
	}

	public void setSelectedIndex(int ix) {
		m_selectedIndex = ix;
		for(int i = getChildCount(); --i >= 0;) {
			getOption(i).setSelected(i == m_selectedIndex);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IHasModifiedIndication impl							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the modified-by-user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#isModified()
	 */
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	public void setModified(boolean as) {
		m_modifiedByUser = as;
	}

}
