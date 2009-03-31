package to.etc.domui.dom.css;

public enum ClearType {
	NONE("none"),
	LEFT("left"),
	RIGHT("right"),
	BOTH("both");

	private String	m_txt;

	ClearType(String s) {
		m_txt = s;
	}
	@Override
	public String toString() {
		return m_txt;
	}
}
