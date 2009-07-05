package to.etc.domui.dom.html;

import to.etc.domui.util.*;

/**
 * The HTML Button tag.
 * 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 19, 2008
 */
public class Button extends NodeContainer {
	private boolean m_disabled;

	private ButtonType m_type = ButtonType.BUTTON;

	private String m_value;

	private char m_accessKey;

	public Button() {
		super("button");
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitButton(this);
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		if(m_disabled != disabled)
			changed();
		m_disabled = disabled;
	}

	public ButtonType getType() {
		return m_type;
	}

	public void setType(ButtonType type) {
		if(m_type != type)
			changed();
		m_type = type;
	}

	public String getValue() {
		return m_value;
	}

	public void setValue(String value) {
		if(!DomUtil.isEqual(value, m_value))
			changed();
		m_value = value;
	}

	public char getAccessKey() {
		return m_accessKey;
	}

	public void setAccessKey(char accessKey) {
		m_accessKey = accessKey;
	}
}
