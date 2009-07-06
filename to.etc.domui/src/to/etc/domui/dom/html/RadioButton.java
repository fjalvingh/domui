package to.etc.domui.dom.html;

import java.util.*;

/**
 *
 * @author <a href="mailto:jo.seaton@itris.nl">Jo Seaton</a>
 * Created on Aug 20, 2008
 */

public class RadioButton extends NodeBase {
	//public class RadioButton extends NodeContainer {

	private boolean m_checked;

	private boolean m_disabled;

	private boolean m_readOnly;

	private String m_name;

	public RadioButton() {
		super("input");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitRadioButton(this);
	}

	public String getName() {
		return m_name;
	}

	public void setName(String s) {
		m_name = s;
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
	}


	@Override
	public void acceptRequestParameter(String[] values) {
		if(values == null || values.length != 1)
			throw new IllegalStateException("RadioButton: expecting a single input value, not " + Arrays.toString(values));

		System.out.println("Value=" + values[0]);
		String s = values[0].trim();
		m_checked = "y".equalsIgnoreCase(s);
	}


}
