package to.etc.domui.dom.html;

public enum ButtonType {
	BUTTON("button"),
	RESET("reset"),
	SUBMIT("submit");

	private String m_txt;

	ButtonType(String t) {
		m_txt = t;
	}
	public String getCode() {
		return m_txt;
	}
}
