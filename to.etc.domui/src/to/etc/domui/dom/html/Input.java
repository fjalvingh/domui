package to.etc.domui.dom.html;

import to.etc.domui.util.*;

/**
 * The "input" tag as a base class. This one only handles classic, non-image inputs.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class Input extends InputNodeBase {
	private boolean m_disabled;

	private int m_maxLength;

	private boolean m_readOnly;

	private int m_size;

	private String m_rawValue;

	private String m_onKeyPressJS;

	public Input() {
		super("input");
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitInput(this);
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		if(m_disabled != disabled)
			changed();
		m_disabled = disabled;
	}

	public int getMaxLength() {
		return m_maxLength;
	}

	public void setMaxLength(int maxLength) {
		if(m_maxLength != maxLength)
			changed();
		m_maxLength = maxLength;
	}

	public boolean isReadOnly() {
		return m_readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		if(m_readOnly != readOnly)
			changed();
		m_readOnly = readOnly;
		if(readOnly)
			addCssClass("ui-ro");
		else
			removeCssClass("ui-ro");
	}

	public int getSize() {
		return m_size;
	}

	public void setSize(int size) {
		if(m_size != size)
			changed();
		m_size = size;
	}

	public String getRawValue() {
		return m_rawValue;
	}

	public void setRawValue(String value) {
		if(!DomUtil.isEqual(value, m_rawValue))
			changed();
		m_rawValue = value;
	}

	public String getOnKeyPressJS() {
		return m_onKeyPressJS;
	}

	public void setOnKeyPressJS(String onKeyPressJS) {
		if(!DomUtil.isEqual(onKeyPressJS, m_onKeyPressJS))
			changed();
		m_onKeyPressJS = onKeyPressJS;
	}

	/**
	 * The input tag accepts a single value.
	 * @see to.etc.domui.dom.html.NodeBase#acceptRequestParameter(java.lang.String[])
	 */
	@Override
	public void acceptRequestParameter(String[] values) {
		if(values == null || values.length != 1)
			m_rawValue = null;
		else
			m_rawValue = values[0];
	}
}
