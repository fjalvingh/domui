package to.etc.domui.dom.html;

public class SelectOption extends NodeContainer {
	private boolean		m_selected;

	private boolean		m_disabled;

	public SelectOption() {
		super("option");
	}

	public SelectOption(String txt) {
		this();
		addLiteral(txt);
	}
	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitOption(this);
	}
	public boolean isSelected() {
		return m_selected;
	}
	public void setSelected(boolean selected) {
		if(m_selected == selected)
			return;
		m_selected = selected;
		changed();
	}
	public boolean isDisabled() {
		return m_disabled;
	}
	public void setDisabled(boolean disabled) {
		if(disabled == m_disabled)
			return;
		m_disabled = disabled;
		changed();
	}
}
