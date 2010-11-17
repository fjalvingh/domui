package to.etc.domui.dom.html;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class TextArea extends InputNodeContainer implements IInputNode<String>, IHasModifiedIndication {
	private int m_cols = -1;

	private int m_rows = -1;

	private String m_value;

	private boolean m_disabled;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	public TextArea() {
		super("textarea");
	}

	public TextArea(int cols, int rows) {
		this();
		m_cols = cols;
		m_rows = rows;
	}


	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTextArea(this);
	}

	public int getCols() {
		return m_cols;
	}

	public void setCols(int cols) {
		if(m_cols == cols)
			return;
		changed();
		m_cols = cols;
	}

	public int getRows() {
		return m_rows;
	}

	public void setRows(int rows) {
		if(m_rows == rows)
			return;
		changed();
		m_rows = rows;
	}

	public boolean validate() {
		if(m_value == null || m_value.length() == 0) {
			if(isMandatory()) {
				setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
				return false;
			}
		}
		return true;
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValue()
	 */
	@Override
	public String getValue() {
		if(!validate())
			throw new ValidationException(Msgs.NOT_VALID, m_value);
		return m_value;
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValueSafe()
	 */
	@Override
	public String getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#hasError()
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}


	public String getRawValue() {
		return m_value;
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		changed();
		m_disabled = disabled;
	}

	@Override
	public void setValue(String v) {
		if(DomUtil.isEqual(v, m_value))
			return;
		m_value = v;
		setText(v);
	}

	@Override
	public boolean acceptRequestParameter(String[] values) throws Exception {
		String nw = (values == null || values.length != 1) ? null : values[0];
		//fixes problem when no data is entered on form and modified flag is raised
		if(nw != null && nw.length() == 0)
			nw = null;
		String cur = m_value != null && m_value.length() == 0 ? null : m_value; // Treat empty string and null the same

		//vmijic 20091124 - some existing entries have \r\n, but after client request roundtrip nw get values with \n instead. Prevent differencies being raised because of this.
		if(cur != null) {
			cur = cur.replaceAll("\r\n", "\n");
		}
		//vmijic 20091126 - now IE returns \r\n, but FF returns \n... So, both nw and cur have to be compared with "\r\n" replaced by "\n"...
		String flattenLineBreaksNw = (nw != null) ? nw.replaceAll("\r\n", "\n") : null;

		//vmijic 20101117 - it is discovered (call 28340) that from some reason firts \n is not rendered in TextArea on client side in initial page render. That cause that same \n is missing from unchanged text area input that comes through client request roundtrip and cause modified flag to be set... So as dirty fix we have to compare without that starting \n too...
		if(flattenLineBreaksNw != null && cur != null && cur.startsWith("\n") && !flattenLineBreaksNw.startsWith("\n")) {
			cur = cur.substring(1);
		}

		if(DomUtil.isEqual(flattenLineBreaksNw, cur))
			return false;

		setValue(nw);
		DomUtil.setModifiedFlag(this);
		return true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IHasModifiedIndication impl							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the modified-by-user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#isModified()
	 */
	@Override
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	@Override
	public void setModified(boolean as) {
		m_modifiedByUser = as;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/

	/** When this is bound this contains the binder instance handling the binding. */
	private SimpleBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	@Override
	public IBinder bind() {
		if(m_binder == null)
			m_binder = new SimpleBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 *
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	@Override
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}
}
