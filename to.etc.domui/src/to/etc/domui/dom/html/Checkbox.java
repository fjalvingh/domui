package to.etc.domui.dom.html;

import java.util.*;

public class Checkbox extends NodeBase {
	private boolean m_checked;

	private boolean m_disabled;

	private boolean m_readOnly;

	public Checkbox() {
		super("input");
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitCheckbox(this);
	}

	public boolean isChecked() {
		return m_checked;
	}

	public void setChecked(boolean checked) {
		if(m_checked != checked)
			changed();
		m_checked = checked;
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		if(m_disabled != disabled)
			changed();
		m_disabled = disabled;
	}

	public boolean isReadOnly() {
		return m_readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		if(m_readOnly != readOnly)
			changed();
		m_readOnly = readOnly;
		m_readOnly = readOnly;
		if(readOnly)
			addCssClass("ui-ro");
		else
			removeCssClass("ui-ro");
	}

	@Override
	public void acceptRequestParameter(String[] values) {
		if(values == null || values.length != 1)
			throw new IllegalStateException("Checkbox: expecting a single input value, not " + Arrays.toString(values));
		String s = values[0].trim();
		m_checked = "y".equalsIgnoreCase(s);
	}

}
