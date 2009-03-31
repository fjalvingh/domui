package to.etc.domui.dom.css;

public enum FloatType {
	NONE("none"),
	LEFT("left"),
	RIGHT("right");

	private String	m_txt;
	
	FloatType(String t) {
		m_txt = t;
	}
	public String getCode() {
		return m_txt;
	}
}
